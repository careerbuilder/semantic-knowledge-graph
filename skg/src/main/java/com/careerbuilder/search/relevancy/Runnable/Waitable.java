package com.careerbuilder.search.relevancy.Runnable;

import org.apache.commons.lang.NotImplementedException;

import java.util.concurrent.Callable;

public abstract class Waitable implements Callable<Waitable>{
   public Exception e = null;

   public Waitable call()
   {
      e = new NotImplementedException();
      throw new NotImplementedException();
   }
}
