fss-propagator.jar command line arguments:

-t		input file path to spacecraft two-line elements
-k		input file path to spacecraft Keplerian elements
-r		input file path to surface elements
-o		output file path (default=output.csv)
-d		simulation duration (days, default=1.0)
-s		simulation time step (minutes, default=1.0)
-f		reference frame (1: Earth inertial (EME2000), 2: Earth inertial (TEME), 3: Earth fixed (ITRF2008) (default=1)
-g		maximum slant range for visibility (meters, default=none)
-e		minimum elevation for visibility (degrees, default=0.0)
-n		network output file path (default=none)