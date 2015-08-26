package com.careerbuilder.search.relevancy.Runnable;

import org.apache.commons.lang.NotImplementedException;

public abstract class Waitable implements Runnable{
   public boolean done = false;
   public Exception e = null;

   public void run()
   {
      e = new NotImplementedException();
      throw new NotImplementedException();
   }

   public void notifyCallers()
   {
      synchronized(this)
      {
         done = true;
         notify();
      }
   }
}
