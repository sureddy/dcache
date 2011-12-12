package dmg.cells.services.multicaster ;
/**
 *  Base event object for the multicaster cell.
 *  Covers 'class' 'name' and reply status and Object.
 *  @author Patrick.Fuhrmann@desy.de
 *  @version cells 2.3
 */
public class MulticastEvent implements java.io.Serializable {
   static final long serialVersionUID = 7167792685678386585L;
   private Object _reply = null ;
   private boolean _ok   = true ;
   private String  _eventClass = null ;
   private String  _eventName  = null ;
   public MulticastEvent( String eventClass , String eventName ){
      _eventClass = eventClass ;
      _eventName  = eventName ;
   }
   public String getEventClass(){ return _eventClass ; }
   public String getEventName(){ return _eventName ; }
   public boolean isOk(){ return _ok ; }
   public void isOk( boolean ok ){ _ok = ok ; }
   public Object getReplyObject(){
      return _reply ;
   }
   public void setReplyObject( Object reply ){ _reply = reply ; }
   public String toString(){
     return _eventClass+":"+_eventName+
            ";ok="+_ok+";"+(_reply!=null?_reply.toString():"") ;
   }
}