package org.apache.commons.collections4.functors;

import org.apache.commons.collections4.Predicate;

public abstract interface PredicateDecorator<T>
  extends Predicate<T>
{
  public abstract Predicate<? super T>[] getPredicates();
}
