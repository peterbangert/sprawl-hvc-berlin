/*

simple_SERVER.sc

- uses two sources for each access point (connected system)
- control azimuth and elevation of each source via OSC

- needs SC-HOA library installed

Henrik von Coler
2021-06-09

*/


/////////////////////////////////////////////////////////////////////////////////
// Server & Setup
/////////////////////////////////////////////////////////////////////////////////

// some server parameters
s.options.device               = "sprawl_SERVER";
s.options.numInputBusChannels  = 32;
s.options.numOutputBusChannels = 2;
s.options.maxLogins            = 4;
s.options.bindAddress          = "0.0.0.0";


// maximum number of access points to be used
~nSystems  = 16;

// number of in/out channels per access point (and jacktrip connection)
~nChannels = 2;

// two sources for each system
~nSources  = ~nChannels*~nSystems;

// HOA parameters
~hoa_order      = 3;
~n_hoa_channels = pow(~hoa_order + 1.0 ,2.0).asInteger;

// kill all other servers
Server.killAll;

s.boot;

s.waitForBoot({
	
		// reverb send level
	~control_reverb_BUS   = Bus.control(s,~nSystems);

	// audio reverb bus
	~reverb_send_BUS      = Bus.audio(s,2);

	/////////////////////////////////////////////////////////////////////////////////
	// Synthdefs: 3rd oder encoder and decoder
	/////////////////////////////////////////////////////////////////////////////////



	SynthDef(\hoa_mono_encoder_3,
	{
		|
		in_bus     = 0,
		out_bus    = 0,
		reverb_bus = 0,
		//
		azim    = 0,
		elev    = 0,
		dist    = 1,
		gain    = 0.5,
		reverb  = 0.1
		|

		var sound = gain * In.ar(in_bus);

		var level =  0.5*min(1,(1/(max(0,dist))));

		var bform = HOASphericalHarmonics.coefN3D(3, azim, elev) * sound * level;

		Out.ar(out_bus, bform);
		Out.ar(reverb_bus, reverb*sound);

	}).add;

	// load HOA stuff for binaural decoder
	HOABinaural.loadbinauralIRs(s);
	s.sync;

	SynthDef(\hoa_binaural_decoder,
		{
			|
			in_bus  = nil, // audio input bus index
			out_bus = nil  // audio output bus index
			|

			var sig = HOABinaural.ar(~hoa_order, In.ar(in_bus,~n_hoa_channels));
			Out.ar(out_bus, sig);

	}).add;

	SynthDef(\convolve,
	{
		|
		bufnum_1 = nil,
		bufnum_2 = nil,
		inbus_1  = 0,
		inbus_2  = 1,
		outbus_1 = 0,
		outbus_2 = 1,
		fftsize  = 1024
		|

		var input1 =   In.ar(inbus_1);
		var input2 =   In.ar(inbus_2);

		Out.ar(outbus_1, PartConv.ar(input1, fftsize, bufnum_1, 0.05));
		Out.ar(outbus_2, PartConv.ar(input2, fftsize, bufnum_2, 0.05));
	}
).add;

	s.sync;



	/////////////////////////////////////////////////////////////////////////////////
	// Encoders & Decoder
	/////////////////////////////////////////////////////////////////////////////////

	// audio bus for the encoded ambisonics signal
	~ambi_BUS      = Bus.audio(s, ~n_hoa_channels);

	// group for all encoders
	~encoder_GROUP = Group.new(s);
	s.sync;

	// add an encoder for each source
	~binaural_encoders = Array.fill(~nSources,	{arg i;

		Synth(\hoa_mono_encoder_3,
			[
				\in_bus,     s.options.numOutputBusChannels + i,
				\out_bus,    ~ambi_BUS.index,
				\reverb_bus, ~reverb_send_BUS
			],
			target: ~encoder_GROUP)
	});
	s.sync;

	~spatial_GROUP = Group.after(~encoder_GROUP);

	s.sync;

	// add one decoder after the encoder group
	~decoder = Synth(\hoa_binaural_decoder,
		[
			\in_bus,  ~ambi_BUS.index,
			\out_bus, 0,
		],
		target: ~spatial_GROUP););
	s.sync;


	/////////////////////////////////////////////////////////////////////////////////
	// Control
	/////////////////////////////////////////////////////////////////////////////////

	// create control buses for angle parameters
	~azim_BUS = Bus.control(s, ~nSources);
	~elev_BUS = Bus.control(s, ~nSources);
	~dist_BUS = Bus.control(s, ~nSources);

	// map buses to encoder parameters
	~binaural_encoders.do({arg e, i; e.map(\azim, ~azim_BUS.index+i)});
	~binaural_encoders.do({arg e, i; e.map(\elev, ~elev_BUS.index+i)});
	~binaural_encoders.do({arg e, i; e.map(\dist, ~dist_BUS.index+i)});
	~binaural_encoders.do({arg e, i; e.map(\reverb, ~control_reverb_BUS.index+i)});


	////////////////////////////////////////////////////////////////////
	// partitioned convolution stuff (to be used with convolve-synthdef)
	////////////////////////////////////////////////////////////////////


	~fftsize = 4096;

	~reverb_FILE =  ~root_DIR++"WAV/IR/kirche_1.wav";

	Buffer.read(s, ~reverb_FILE);

	s.sync;

	~conv_func_L =  {

		var ir, irbuffer, bufsize;

		irbuffer = Buffer.readChannel(s, ~reverb_FILE, channels: [0]);

		s.sync;

		bufsize = PartConv.calcBufSize(~fftsize, irbuffer);

		// ~numpartitions= PartConv.calcNumPartitions(~fftsize, irbuffer);

		~irspectrumL = Buffer.alloc(s, bufsize, 1);
		~irspectrumL.preparePartConv(irbuffer, ~fftsize);

		s.sync;

		irbuffer.free;

	}.fork;

	s.sync;

	2.sleep;


	~conv_func_R =  {

		var ir, irbuffer, bufsize;

		irbuffer = Buffer.readChannel(s, ~reverb_FILE, channels: [1]);

		s.sync;

		bufsize = PartConv.calcBufSize(~fftsize, irbuffer);

		// ~numpartitions= PartConv.calcNumPartitions(~fftsize, irbuffer);

		~irspectrumR = Buffer.alloc(s, bufsize, 1);
		~irspectrumR.preparePartConv(irbuffer, ~fftsize);

		s.sync;
		irbuffer.free;

	}.fork;

	s.sync;


	2.sleep;

	postln('Adding convolution reverb!');
	~conv = Synth.new(\convolve,
		[
			\outbus_1, 0,
			\outbus_2, 1,
			\bufnum_1, ~irspectrumL.bufnum,
			\bufnum_2, ~irspectrumR.bufnum,
			\fftsize,  ~fftsize
		],
		target: ~spatial_GROUP);

	s.sync;

	~conv.set(\inbus_1, ~reverb_send_BUS.index);
	~conv.set(\inbus_2, ~reverb_send_BUS.index);

	~conv.set(\outbus_1, 82);
	~conv.set(\outbus_2, 83);


	thisProcess.openUDPPort(57121);
	"listening for OSC on ports: ".post;
	thisProcess.openPorts.postln;

	// OSC listener for azimuth
	OSCdef('azim',
		{
			arg msg, time, addr, recvPort;
			var azim = msg[2];
			~azim_BUS.setAt(msg[1], azim);
	    }, '/source/azim');

	// OSC listener for elevation<
	OSCdef('elev',
		{
			arg msg, time, addr, recvPort;
			var elev = msg[2];
			~elev_BUS.setAt(msg[1], elev);
	    }, '/source/elev');

	// OSC listener for distance
	OSCdef('dist',
		{
			arg msg, time, addr, recvPort;
			var dist = msg[2];
			~dist_BUS.setAt(msg[1], dist);
	    }, '/source/dist');

	// OSC listener for reverb
	OSCdef('reverb',
		{
			arg msg, time, addr, recvPort;
			var reverb = msg[2];
			~control_reverb_BUS.setAt(msg[1], reverb);
	    }, '/source/reverb');

	///////////////////////////////////////////////////////////////////////a//////////
	// Broadcast
	/////////////////////////////////////////////////////////////////////////////////
	~broadcast_receiver = NetAddr("127.0.0.1", 5005);

	~broadcast_interval = 0.01;

	~broadcast_OSC_ROUTINE = Routine({
		inf.do({
			var azim, elev, dist;
			for (0, ~nSources-1, {
			 	arg i;

			 	azim = ~azim_BUS.getnSynchronous(~nInputs)[i];
			 	elev = ~elev_BUS.getnSynchronous(~nInputs)[i];
			 	dist = ~dist_BUS.getnSynchronous(~nInputs)[i];

				~broadcast_receiver.sendMsg('/source/aed', i, azim, elev, dist);
			});
			~broadcast_interval.wait;
		});
	});

	~broadcast_OSC_ROUTINE.play;

});
