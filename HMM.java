import java.io.*;
import java.util.*;

//
// Hidden Markov Model
//


public class HMM
{

  //
  // this method does most of the work here
  //
  public void run (String observedEmissions)
  {
    // holds our matrix
    int LENGTH = observedEmissions.length();
    double [][] array = new double[NUM_STATES][LENGTH];

    // holds our pointers
    int [] ptr = new int [LENGTH];

    // initialization
    for (int i = 0; i < NUM_STATES; i++) 
      for (int j = 0; j < LENGTH; j++)
        array[i][j] = Double.NEGATIVE_INFINITY;
    
    array[A][0] = 1.0;


    // for each successive observation, populate the matrix
    for (int i = 1; i < LENGTH; i++)
    {
      char observed = observedEmissions.charAt(i);

      array[A][i] = Math.log(e(A, observed)) + sum (A, array, i);
      array[B][i] = Math.log(e(B, observed)) + sum (B, array, i);
      array[C][i] = Math.log(e(C, observed)) + sum (C, array, i);
      array[E][i] = Math.log(e(E, observed)) + sum (E, array, i);
    }
    printArray (array, LENGTH - 1);

    for (int i = 0; i < LENGTH; i++)
    {
      char observed = observedEmissions.charAt(i);
      ptr[i] = argmax (observed, array, i);
    }



    // print out the ptr
    System.out.println (observedEmissions + ":");
    for (int i = 0; i < LENGTH; i++) System.out.print ("  " + toState(ptr[i]));
    System.out.println();
    for (int i = 0; i < LENGTH; i++) System.out.print ("  " + observedEmissions.charAt(i));
    System.out.println();
    System.out.println();
  }



  private double sum (int state, double [][] array, int currentObservationIndex)
  {
    double ret = Double.NEGATIVE_INFINITY;

    for (int tempState = A; tempState < NUM_STATES; tempState++)
    {
      double fki = array[tempState][currentObservationIndex - 1];
      double akl = akl(state, tempState);

      if (akl != Double.NEGATIVE_INFINITY && fki != Double.NEGATIVE_INFINITY)
      {
        if (ret == Double.NEGATIVE_INFINITY) ret = 0.0;
        double total = fki + Math.log(akl);
        ret += total;
      }
    }

    return ret;
  }



  private double akl (int prev, int next)
  {
    double ret = Double.NEGATIVE_INFINITY;

    // if (prev < next)      return 0.0;  // can't go back
    // if (prev > (next + 2))  return 0.0;  // can't jump ahead
    if (prev == next)    ret = 0.7;  // stays the same
    if (prev == (next + 1))  ret = 0.3;  // going to the next state

    return ret;  
  }





  


  private static char toState (int i)
  {
    switch (i)
    {
      case A:  return STATE_A;
      case B:  return STATE_B;
      case C:  return STATE_C;
      default: return STATE_E;
    }
  }


  // calculates argmax 
  private int argmax (int curstate, double [][] array, int index)
  {
    double maxval = Double.NEGATIVE_INFINITY;
    int max = 0;

    for (int i = 0; i < NUM_STATES; i++)
    {
      double foo = array[i][index];
      if (foo > maxval)
      {
        max = i;
        maxval = foo;
      }
    }

    return max;
  }



  // returns the chance of observing something
  private double e (int l, char xi)  // state, observed
  {
    switch (l)
    {
      case A:
        switch (xi)
        {
          case EMIT_ZERO: return 0.5;
          case EMIT_ONE:  return 0.5;
          default:        return 0.0;
        }
      case B:
        switch (xi)
        {
          case EMIT_ZERO: return 0.1;
          case EMIT_ONE:  return 0.8;
          case EMIT_Q:    return 0.1;
          default:        return 0.0;
        }
      case C:
        switch (xi)
        {
          case EMIT_ZERO: return 0.5;
          case EMIT_ONE:  return 0.5;
          default:        return 0.0;
        }
      default:
        return 0.0;
    }
  }



  private void printArray (double [][] array, int observedCount)
  {
    System.out.println ("array " + observedCount + ":");

    for (int i = 0; i < NUM_STATES; i++)
    {
      System.out.print (toState(i) + ": ");
      for (int j = 0; j <= observedCount; j++)
      {
        System.out.print ("  " + shorten(array[i][j]));
      }
      System.out.println();
    }

    System.out.println();
  }  


  private String shorten (double x)
  {
    int LIMIT = 5;

    if (x == Double.NEGATIVE_INFINITY) return "-Inf ";

    String str = Double.toString(x);
    if (str.length() <= LIMIT) return str;

    return str.substring(0, LIMIT);
  }



  // main
  public static void main (String [] args)
  {
    try
    {
      System.out.println();
      HMM hmm = new HMM ();
      hmm.run("00000000");
      hmm.run("0010?1110100");
      hmm.run("00101101110");
      hmm.run("1100110?00111110");
    }
    catch (Throwable e)
    {
      e.printStackTrace();
      System.out.println (e.getClass().getName() + " " + e.getMessage());
    }
  }




  // these are our emissions
  private static final char EMIT_ZERO = '0';
  private static final char EMIT_ONE  = '1';
  private static final char EMIT_Q    = '?';

  // these are our states
  private static final char STATE_A   = 'A';
  private static final char STATE_B   = 'B';
  private static final char STATE_C   = 'C';
  private static final char STATE_E   = 'E';

  private static final int A          = 0;
  private static final int B          = 1;
  private static final int C          = 2;
  private static final int E          = 3;
  private static final int NUM_STATES = 4;



  public class A extends State
  {
    public char getLabel ()   { return STATE_A; }

    public A ()
    {
      super (STATE_A, 0.7);
    }
  }


  //
  // INNER CLASSES
  //
  public class State 
  {

    //
    // object behaviors
    //

    // returns the next state
    public State randomTransition ()
    {
      double rnd = Math.random();
      if (rnd <= getTransitionProbability()) return new State (getLabel());
      else return nextState();
    }

    private State nextState ()
    {
      if (myLabel == STATE_A) return new State (STATE_B);
      if (myLabel == STATE_B) return new State (STATE_C);
      if (myLabel == STATE_C) return new State (STATE_E);
      return null;
    }
    

    // emits a random char, according to the probabilities
    public char randomEmission ()
    {
      double rnd = Math.random();
      if (rnd <= p0()) return EMIT_ZERO;
      if (rnd <= p0() + p1()) return EMIT_ONE;
      return EMIT_Q;
    }

  
    // return the chance of emitting the three things
    public double p0 ()
    {
      if (myLabel == STATE_A) return 0.5;
      if (myLabel == STATE_B) return 0.1;
      if (myLabel == STATE_C) return 0.7;
      return 0.0;
    }
    public double p1 ()
    {
      if (myLabel == STATE_A) return 0.5;
      if (myLabel == STATE_B) return 0.8;
      if (myLabel == STATE_C) return 0.3;
      return 0.0;
    }
    public double pQ ()
    {
      if (myLabel == STATE_A) return 0.0;
      if (myLabel == STATE_B) return 0.1;
      if (myLabel == STATE_C) return 0.0;
      return 0.0;
    }



    //
    // object attributes
    //
    public double getTransitionProbability ()         { return myTransitionProbability; }
    public void   setTransitionProbability (double x) { myTransitionProbability = x; }

    public char getLabel ()         { return myLabel; }
    public void setLabel (char x)   { myLabel = x; }



    //
    // object bookkeepers
    //
    public State (char theLabel, double theTransitionProbability)
    {
      myLabel = theLabel;
      myTransitionProbability = theTransitionProbability;
    }
    public State (char theLabel)
    {
      myLabel = theLabel;
      myTransitionProbability = 0.7;
    }



    // privates
    private char  myLabel;
    private double myTransitionProbability;
  }


}  
