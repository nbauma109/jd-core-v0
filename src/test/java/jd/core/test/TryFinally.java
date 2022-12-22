package jd.core.test;

public class TryFinally {
    
    boolean flag;

    public void tryFinally(boolean value)
    {
      setFlag(true);
      try {
          setFlag(true);
          try {
            setFlag(value);
          } finally {
            setFlag(false);
          }
        setFlag(value);
      } finally {
        setFlag(false);
      }
    }

    public void tryEmptyCatchFinally(boolean value)
    {
        setFlag(true);
        try {
            setFlag(true);
            try {
                setFlag(value);
            } catch (Exception e) {
            } finally {
                setFlag(true);
            }
        } catch (Exception e) {
        } finally {
            setFlag(false);
        }
    }
    
    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
