SynthDef(\clickedSin, {
	arg note = 38, dec = 1, amp = 1, out = 0, pos = 0;
	var sin, sig;

	sin =  Mix.ar( FSinOsc.ar(freq: [note].midicps,  mul: 0.05).tanh);
	sig = sin + (LPF.ar(Impulse.ar(0, mul: 1), 1000) * 0.4);

	sig = HPF.ar(in: sig, freq: 20) * EnvGen.kr(Env.perc(attackTime:0, releaseTime:dec), doneAction: 2) * 10 * amp;
	sig = Clip.ar(in:sig,lo:-1,hi:1);

	Out.ar( out, Pan2.ar(sig, pos));
}).store;
