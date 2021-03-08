import waba.ui.*;
import waba.sys.*;

//
//  WabaMark.java
//  
//
//  Created by Sean Luke (sean@cs.gmu.edu) on Sat May 19 2001,
//  gathered from open-source license-free stuff on the web.
//  Do what you will with this.
//

/*
Compiled under MacOS X for various platforms using the latest wababin
code from Steve Weyer.  Grab the wababin.jar file from the Waba for
Newton website.

jikes WabaMark.java				# you could use javac
java wababin.Warp c WabaMark *.class
java wababin.Exegen /m 30000 WabaMark WabaMark WabaMark

*/


// this generator is significnatly broken, and I have a better one,
// but I was too lazy to toss it in here.  It's pretty dang 
// nonrandom, but I doubt that'll effect the results where it's used
 class WabaMarkRandom
  {
  public static final  int A = 9301;
  public static final  int C = 49297;
  public static final  int M = 233280;
  public int seed;
  
    // return random number between 0 and 1 and update seed:
    float nextfloat()
    {
      seed = (seed * A + C) % M;
      return (float) seed / (float)M;
    }

    public WabaMarkRandom(int Iseed)  // -- ugh, this is ugly, we'll get bad
                // maybe negative results because we're using ints and 
                // not longs, but whatever
        { seed = Iseed; }

  }
  
  // Initialize the constants:


class WabaMarkTestObject
    {
    int x;
    public WabaMarkTestObject() { x = 3; }
    }
    
public class WabaMark extends MainWindow 
    {
    public Button pushB;
    public Label sieve;
    public Label whetstone;
    public Label linpack;
    
    public static final int SIEVE_ITERATIONS = 75;  // 200
    public static final int WHETSTONE_ITERATIONS = 20;  // 100
    public static final int LINPACK_ITERATIONS = 15; // 20
    public String stext;
    public String wtext;
    public String ltext;
    
    public void onStart()
        {
        pushB = new Button("Start Benchmark");
        pushB.setRect(10, 10, 100, 20);
        add(pushB);
        stext = "Sieve:";
        wtext = "Whetstone:";
        ltext = "Linpack:";
        sieve = new Label(stext);
        whetstone = new Label(wtext);
        linpack = new Label(ltext);
        sieve.setRect(10,30,100,20);
        whetstone.setRect(10,50,100,20);
        linpack.setRect(10,70,100,20);
        add(sieve); add(whetstone); add(linpack); 
        }
 
    public void onEvent(Event event)
        {
        if (event.type == ControlEvent.PRESSED &&
        event.target == pushB)
            {
        stext = "Sieve:";
        wtext = "Whetstone:";
        ltext = "Linpack:";
        sieve.setText(stext);
        whetstone.setText(wtext);
        linpack.setText(ltext);
                _doPaint(0,0,160,160);
            int stamp;
            int stamp2;
            
            stamp = Vm.getTimeStamp();
            totest = new short[SIEVE_LIMIT]; 
            flags  = new short[SIEVE_LIMIT]; 
            for(int x=0;x<SIEVE_ITERATIONS;x++) sieve();
            totest = null;  // save space
            flags = null;
            stamp2 = Vm.getTimeStamp();
            sieve.setText("Sieve: " + (stamp2-stamp));
            _doPaint(0,0,160,160);
           
            stamp = Vm.getTimeStamp();
            e1 = new float[100];
            z = new float[1];
            whetstone(WHETSTONE_ITERATIONS);
            e1 = null;
            z = null;
            stamp2 = Vm.getTimeStamp();
            whetstone.setText("Whetstone: " + (stamp2-stamp));
            _doPaint(0,0,160,160);

            stamp = Vm.getTimeStamp();
            for(int x=0;x<20;x++) linpack(LINPACK_ITERATIONS);
            stamp2 = Vm.getTimeStamp();
            linpack.setText("Linpack: " + (stamp2-stamp));
                _doPaint(0,0,160,160);
            }
        }
    
    public static final int SIEVE_LIMIT=1000;
        
    short [] totest; // List of numbers to test
    short [] flags;   //Flags a number as prime or not

    public void sieve()
        {
        int limit = SIEVE_LIMIT;  // same as totest and flags

        // find the square root of limit, plus 1
        short square_root;
        for(square_root=0;square_root<limit;square_root++)
            if ((square_root*square_root)>=limit) break;
                                                 
        short counter, multiple, temp=0;
        short one=1; //Interesting sentinel value :)

        for(counter=1; counter<limit; counter++)
            {       
            totest[counter]=counter; flags[counter]=1;
            } // Set all prime
    
        counter=2;
        multiple=2;

        while(one==1)
            {
            if(totest[counter]>=square_root+1) one=2;

            while(temp<limit)
                {
                temp=(short)(totest[counter] * multiple);
                if(temp<limit) { flags[temp]=0;  }  // End if conditional
                multiple++;
                } //End while loop

            multiple=2; //Reset values
            temp=1;  // reset values
            counter++;
            } //End while loop
        // at this point we have all the prime numbers in flags.
        }
        
    
        
 float		x1, x2, x3, x4, x, y, z[], t, t1, t2;
 float 		e1[];
 int		i, j, k, l, n1, n2, n3, n4, n6, n7, n8, n9, n10, n11;

    public void whetstone(int iterations)
   { 	

        /* initialize constants */
	/*System.out.println("Start");*/
	t   =   0.499975f;
	t1  =   0.50025f;
	t2  =   2.0f;

	/* set values of module weights */

	n1  =   0 * iterations;
	n2  =  12 * iterations;
	n3  =  14 * iterations;
	n4  = 345 * iterations;
	n6  = 210 * iterations;
	n7  =  32 * iterations;
	n8  = 899 * iterations;
	n9  = 616 * iterations;
	n10 =   0 * iterations;
	n11 =  93 * iterations;

/* MODULE 1:  simple identifiers */

	x1 =  1.0f;
	x2 = x3 = x4 = -1.0f;

	for(i = 1; i <= n1; i += 1) {
		x1 = ( x1 + x2 + x3 - x4 ) * t;
		x2 = ( x1 + x2 - x3 - x4 ) * t;
		x3 = ( x1 - x2 + x3 + x4 ) * t;
		x4 = (-x1 + x2 + x3 + x4 ) * t;
	}


/* MODULE 2:  array elements */

	e1[0] =  1.0f;
	e1[1] = e1[2] = e1[3] = -1.0f;

	for (i = 1; i <= n2; i +=1) {
		e1[0] = ( e1[0] + e1[1] + e1[2] - e1[3] ) * t;
		e1[1] = ( e1[0] + e1[1] - e1[2] + e1[3] ) * t;
		e1[2] = ( e1[0] - e1[1] + e1[2] + e1[3] ) * t;
		e1[3] = (-e1[0] + e1[1] + e1[2] + e1[3] ) * t;
	}


/* MODULE 3:  array as parameter */

	for (i = 1; i <= n3; i += 1)
		pa(e1);


/* MODULE 4:  conditional jumps */

	j = 1;
	for (i = 1; i <= n4; i += 1) {
		if (j == 1)
			j = 2;
		else
			j = 3;

		if (j > 2)
			j = 0;
		else
			j = 1;

		if (j < 1 )
			j = 1;
		else
			j = 0;
	}


/* MODULE 5:  omitted */

/* MODULE 6:  integer arithmetic */

	j = 1;
	k = 2;
	l = 3;

	for (i = 1; i <= n6; i += 1) {
		j = j * (k - j) * (l -k);
		k = l * k - (l - j) * k;
		l = (l - k) * (k + j);

		e1[l - 2] = j + k + l;		/* C arrays are zero based */
		e1[k - 2] = j * k * l;
	}


/* MODULE 7:  trig. functions */  // commented out, Waba doesn't have math functions by default
//	x = y = 0.5;

//	for(i = 1; i <= n7; i +=1) {
//		x = t * Math.atan(t2*Math.sin(x)*Math.cos(x)/(Math.cos(x+y)+Math.cos(x-y)-1.0));
//		y = t * Math.atan(t2*Math.sin(y)*Math.cos(y)/(Math.cos(x+y)+Math.cos(x-y)-1.0));
//	}

/* MODULE 8:  procedure calls */

	x = y = z[0] = 1.0f;

	for (i = 1; i <= n8; i +=1)
                p3(x, y, z);

/* MODULE9:  array references */

	j = 1;
	k = 2;
	l = 3;

	e1[0] = 1.0f;
	e1[1] = 2.0f;
	e1[2] = 3.0f;

	for(i = 1; i <= n9; i += 1)
		p0();

/* MODULE10:  integer arithmetic */

	j = 2;
	k = 3;

	for(i = 1; i <= n10; i +=1) {
		j = j + k;
		k = j + k;
		j = k - j;
		k = k - j - j;
	}

/* MODULE11:  standard functions */

//	x = 0.75;
//	for(i = 1; i <= n11; i +=1)
//		x = Math.sqrt( Math.exp( Math.log(x) / t1));
    }
    
public void pa(float e[])
{
	int j;

	j = 0;
     do {
	e[0] = (  e[0] + e[1] + e[2] - e[3] ) * t;
	e[1] = (  e[0] + e[1] - e[2] + e[3] ) * t;
	e[2] = (  e[0] - e[1] + e[2] + e[3] ) * t;
	e[3] = ( -e[0] + e[1] + e[2] + e[3] ) / t2;
	j += 1;}
	while (j < 6);
	
}


public void p3(float x,float y,float z[])
{
	x  = t * (x + y);
	y  = t * (x + y);
	z[0] = (x + y) /t2;
}


public void p0()
{
	e1[j] = e1[k];
	e1[k] = e1[l];
	e1[l] = e1[j];
}

        
        
        
        
        
        
        
        
        
        
        




// ---- LINPACK
 
  float mflops_result = 0.0f;
  float residn_result = 0.0f;
  float time_result = 0.0f;
  float eps_result = 0.0f;
  float total = 0.0f;

  final float abs (float d) {
    return (d >= 0) ? d : -d;
  }


  public void linpack (int n)
  {
    int ldaa = n*2;
    int lda = ldaa+1;
    float a[][] = new float[ldaa][lda];
    float b[] = new float[ldaa];
    float x[] = new float[ldaa];
    float cray,ops,norma,normx;
    float resid,time;
    float kf;
    int i,ntimes,info,kflops;
    int ipvt[] = new int[ldaa];
    
    cray = .056f;
    
    ops = (2.0e0f*(n*n*n))/3.0f + 2.0f*(n*n);
    
    /* Norm a == max element. */
    norma = matgen(a,lda,n,b);

    /* Factor a.  */
    info = dgefa(a,lda,n,ipvt);

    /* Solve ax=b. */
    dgesl(a,lda,n,ipvt,b,0);
    
    for (i = 0; i < n; i++) {
      x[i] = b[i];
    }

    norma = matgen(a,lda,n,b);

    for (i = 0; i < n; i++) {
      b[i] = -b[i];
    }

    dmxpy(n,b,n,lda,x,a);

    resid = 0.0f;
    normx = 0.0f;

    for (i = 0; i < n; i++) {
      resid = (resid > abs(b[i])) ? resid : abs(b[i]);
      normx = (normx > abs(x[i])) ? normx : abs(x[i]);
    }
    
    eps_result = epslon(1.0f);
  }
  

  
  final float matgen (float a[][], int lda, int n, float b[])
  {
    WabaMarkRandom gen;
    float norma;
    int init, i, j;
    
    init = 1325;
    norma = 0.0f;

    gen = new WabaMarkRandom(init);

    /*  Next two for() statements switched.  Solver wants
     *  matrix in column order. --dmd 3/3/97
     */
    for (i = 0; i < n; i++) {
      for (j = 0; j < n; j++) {
	a[j][i] = gen.nextfloat() - .5f;
	norma = (a[j][i] > norma) ? a[j][i] : norma;
      }
    }

    for (i = 0; i < n; i++) {
      b[i] = 0.0f;
    }

    for (j = 0; j < n; j++) {
      for (i = 0; i < n; i++) {
	b[i] += a[j][i];
      }
    }
    
    return norma;
  }
  
  /*
    dgefa factors a float precision matrix by gaussian elimination.
    
    dgefa is usually called by dgeco, but it can be called
    directly with a saving in time if  rcond  is not needed.
    (time for dgeco) = (1 + 9/n)*(time for dgefa) .
    
    on entry
    
    a       float precision[n][lda]
    the matrix to be factored.
    
    lda     integer
    the leading dimension of the array  a .
    
    n       integer
    the order of the matrix  a .
    
    on return
    
    a       an upper triangular matrix and the multipliers
    which were used to obtain it.
    the factorization can be written  a = l*u  where
    l  is a product of permutation and unit lower
    triangular matrices and  u  is upper triangular.
    
    ipvt    integer[n]
    an integer vector of pivot indices.
    
    info    integer
    = 0  normal value.
    = k  if  u[k][k] .eq. 0.0 .  this is not an error
    condition for this subroutine, but it does
    indicate that dgesl or dgedi will divide by zero
    if called.  use  rcond  in dgeco for a reliable
    indication of singularity.
    
    linpack. this version dated 08/14/78.
    cleve moler, university of new mexico, argonne national lab.
    
    functions
    
    blas daxpy,dscal,idamax
  */

  final int dgefa( float a[][], int lda, int n, int ipvt[])
  {
    float[] col_k, col_j;
    float t;
    int j,k,kp1,l,nm1;
    int info;
    
    // gaussian elimination with partial pivoting
    
    info = 0;
    nm1 = n - 1;
    if (nm1 >=  0) {
      for (k = 0; k < nm1; k++) {
	col_k = a[k];
	kp1 = k + 1;
	
	// find l = pivot index
	
	l = idamax(n-k,col_k,k,1) + k;
	ipvt[k] = l;
	
	// zero pivot implies this column already triangularized
	
	if (col_k[l] != 0) {
	  
	  // interchange if necessary
	  
	  if (l != k) {
	    t = col_k[l];
	    col_k[l] = col_k[k];
	    col_k[k] = t;
	  }
	  
	  // compute multipliers
	  
	  t = -1.0f/col_k[k];
	  dscal(n-(kp1),t,col_k,kp1,1);
	  
	  // row elimination with column indexing
	  
	  for (j = kp1; j < n; j++) {
	    col_j = a[j];
	    t = col_j[l];
	    if (l != k) {
	      col_j[l] = col_j[k];
	      col_j[k] = t;
	    }
	    daxpy(n-(kp1),t,col_k,kp1,1,
		  col_j,kp1,1);
	  }
	}
	else {
	  info = k;
	}
      }
    }

    ipvt[n-1] = n-1;
    if (a[(n-1)][(n-1)] == 0) info = n-1;
    
    return info;
  }

  
  
  /**
   * dgesl solves the float precision system
   * a * x = b  or  trans(a) * x = b
   * using the factors computed by dgeco or dgefa.
   *
   * on entry
   *
   * a       float precision[n][lda]
   * the output from dgeco or dgefa.
   *
   * lda     integer
   * the leading dimension of the array  a .
   *
   * n       integer
   * the order of the matrix  a .
   *
   * ipvt    integer[n]
   * the pivot vector from dgeco or dgefa.
   *
   * b       float precision[n]
   * the right hand side vector.
   *
   * job     integer
   * = 0         to solve  a*x = b ,
   * = nonzero   to solve  trans(a)*x = b  where
   * trans(a)  is the transpose.
   *
   * on return
   *
   * b       the solution vector  x .
   *
   * error condition
   *
   * a division by zero will occur if the input factor contains a
   * zero on the diagonal.  technically this indicates singularity
   * but it is often caused by improper arguments or improper
   * setting of lda .  it will not occur if the subroutines are
   * called correctly and if dgeco has set rcond .gt. 0.0
   * or dgefa has set info .eq. 0 .
   *
   * to compute  inverse(a) * c  where  c  is a matrix
   * with  p  columns
   * dgeco(a,lda,n,ipvt,rcond,z)
   * if (!rcond is too small){
   * for (j=0,j<p,j++)
   * dgesl(a,lda,n,ipvt,c[j][0],0);
   * }
   * 
   * linpack. this version dated 08/14/78 .
   * cleve moler, university of new mexico, argonne national lab.
   * 
   * functions
   * 
   * blas daxpy,ddot
   **/

  final void dgesl( float a[][], int lda, int n, int ipvt[], float b[], int job)
  {
    float t;
    int k,kb,l,nm1,kp1;

    nm1 = n - 1;
    if (job == 0) {

      // job = 0 , solve  a * x = b.  first solve  l*y = b

      if (nm1 >= 1) {
	for (k = 0; k < nm1; k++) {
	  l = ipvt[k];
	  t = b[l];
	  if (l != k){
	    b[l] = b[k];
	    b[k] = t;
	  }
	  kp1 = k + 1;
	  daxpy(n-(kp1),t,a[k],kp1,1,b,kp1,1);
	}
      }

      // now solve  u*x = y

      for (kb = 0; kb < n; kb++) {
	k = n - (kb + 1);
	b[k] /= a[k][k];
	t = -b[k];
	daxpy(k,t,a[k],0,1,b,0,1);
      }
    }
    else {

      // job = nonzero, solve  trans(a) * x = b.  first solve  trans(u)*y = b

      for (k = 0; k < n; k++) {
	t = ddot(k,a[k],0,1,b,0,1);
	b[k] = (b[k] - t)/a[k][k];
      }

      // now solve trans(l)*x = y 

      if (nm1 >= 1) {
	//for (kb = 1; kb < nm1; kb++) {
	for (kb = 0; kb < nm1; kb++) {
	  k = n - (kb+1);
	  kp1 = k + 1;
	  b[k] += ddot(n-(kp1),a[k],kp1,1,b,kp1,1);
	  l = ipvt[k];
	  if (l != k) {
	    t = b[l];
	    b[l] = b[k];
	    b[k] = t;
	  }
	}
      }
    }
  }



  /**
   * constant times a vector plus a vector.
   * jack dongarra, linpack, 3/11/78.
   **/

  final void daxpy( int n, float da, float dx[], int dx_off, int incx,
	      float dy[], int dy_off, int incy)
  {
    int i,ix,iy;

    if ((n > 0) && (da != 0)) {
      if (incx != 1 || incy != 1) {

	// code for unequal increments or equal increments not equal to 1

	ix = 0;
	iy = 0;
	if (incx < 0) ix = (-n+1)*incx;
	if (incy < 0) iy = (-n+1)*incy;
	for (i = 0;i < n; i++) {
	  dy[iy +dy_off] += da*dx[ix +dx_off];
	  ix += incx;
	  iy += incy;
	}
	return;
      } else {

	// code for both increments equal to 1

	for (i=0; i < n; i++)
	  dy[i +dy_off] += da*dx[i +dx_off];
      }
    }
  }



  /**
   * forms the dot product of two vectors.
   * jack dongarra, linpack, 3/11/78.
   **/

  final float ddot( int n, float dx[], int dx_off, int incx, float dy[],
	       int dy_off, int incy)
  {
    float dtemp;
    int i,ix,iy;

    dtemp = 0;

    if (n > 0) {
      
      if (incx != 1 || incy != 1) {

	// code for unequal increments or equal increments not equal to 1

	ix = 0;
	iy = 0;
	if (incx < 0) ix = (-n+1)*incx;
	if (incy < 0) iy = (-n+1)*incy;
	for (i = 0;i < n; i++) {
	  dtemp += dx[ix +dx_off]*dy[iy +dy_off];
	  ix += incx;
	  iy += incy;
	}
      } else {

	// code for both increments equal to 1
	
	for (i=0;i < n; i++)
	  dtemp += dx[i +dx_off]*dy[i +dy_off];
      }
    }
    return(dtemp);
  }

  /**
   * scales a vector by a constant.
   * jack dongarra, linpack, 3/11/78.
   **/

  final void dscal( int n, float da, float dx[], int dx_off, int incx)
  {
    int i,nincx;

    if (n > 0) {
      if (incx != 1) {

	// code for increment not equal to 1

	nincx = n*incx;
	for (i = 0; i < nincx; i += incx)
	  dx[i +dx_off] *= da;
      } else {

	// code for increment equal to 1

	for (i = 0; i < n; i++)
	  dx[i +dx_off] *= da;
      }
    }
  }
  
  /**
   * finds the index of element having max. absolute value.
   * jack dongarra, linpack, 3/11/78.
   **/

  final int idamax( int n, float dx[], int dx_off, int incx)
  {
    float dmax, dtemp;
    int i, ix, itemp=0;

    if (n < 1) {
      itemp = -1;
    } else if (n ==1) {
      itemp = 0;
    } else if (incx != 1) {

      // code for increment not equal to 1

      dmax = (dx[dx_off] < 0.0f) ? -dx[dx_off]: dx[dx_off];
      ix = 1 + incx;
      for (i = 0; i < n; i++) {
	dtemp = (dx[ix + dx_off] < 0.0f) ? -dx[ix + dx_off]: dx[ix + dx_off];
	if (dtemp > dmax)  {
	  itemp = i;
	  dmax = dtemp;
	}
	ix += incx;
      }
    } else {

      // code for increment equal to 1

      itemp = 0;
      dmax = (dx[dx_off] < 0.0f)? -dx[dx_off] : dx[dx_off];
      for (i = 0; i < n; i++) {
	dtemp = (dx[i + dx_off] < 0.0f) ? -dx[i+dx_off]: dx[i+dx_off];
	if (dtemp > dmax) {
	  itemp = i;
	  dmax = dtemp;
	}
      }
    }
    return (itemp);
  }
  
  /**
   * estimate unit roundoff in quantities of size x.
   * 
   * this program should function properly on all systems
   * satisfying the following two assumptions,
   * 1.  the base used in representing dfloating point
   * numbers is not a power of three.
   * 2.  the quantity  a  in statement 10 is represented to
   * the accuracy used in dfloating point variables
   * that are stored in memory.
   * the statement number 10 and the go to 10 are intended to
   * force optimizing compilers to generate code satisfying
   * assumption 2.
   * under these assumptions, it should be true that,
   * a  is not exactly equal to four-thirds,
   * b  has a zero for its last bit or digit,
   * c  is not exactly equal to one,
   * eps  measures the separation of 1.0 from
   * the next larger dfloating point number.
   * the developers of eispack would appreciate being informed
   * about any systems where these assumptions do not hold.
   * 
   * *****************************************************************
   * this routine is one of the auxiliary routines used by eispack iii
   * to avoid machine dependencies.
   * *****************************************************************
   * 
   *   this version dated 4/6/83.
   **/

  final float epslon (float x)
  {
    float a,b,c,eps;

    a = 4.0e0f/3.0e0f;
    eps = 0;
    while (eps == 0) {
      b = a - 1.0f;
      c = b + b + b;
      eps = abs(c-1.0f);
    }
    return(eps*abs(x));
  }

  /**
   * purpose:
   * multiply matrix m times vector x and add the result to vector y.
   * 
   * parameters:
   * 
   * n1 integer, number of elements in vector y, and number of rows in
   * matrix m
   * 
   * y float [n1], vector of length n1 to which is added
   * the product m*x
   * 
   * n2 integer, number of elements in vector x, and number of columns
   * in matrix m
   * 
   * ldm integer, leading dimension of array m
   * 
   * x float [n2], vector of length n2
   * 
   * m float [ldm][n2], matrix of n1 rows and n2 columns
   **/

  final void dmxpy ( int n1, float y[], int n2, int ldm, float x[], float m[][])
  {
    int j,i;

    // cleanup odd vector
    for (j = 0; j < n2; j++) {
      for (i = 0; i < n1; i++) {
	y[i] += x[j]*m[j][i];
      }
    }
  }



        
    }
    
    
    