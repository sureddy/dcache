package dmg.cells.applets ;

import java.applet.*;
import java.awt.* ;
import java.awt.event.* ;
import java.net.* ;
import java.io.*;
import dmg.util.* ;
import dmg.protocols.ssh.* ;

public class      SshClientFPPanel 
       extends    SshClientActionPanel 
       implements ActionListener {
       
   private Button    _acceptButton ;
   private Button    _rejectButton ;
   private Label     _hostLabel ;
   private Label     _fingerprintLabel ;
   private Font      _font = new Font( "TimesRoman" , Font.BOLD , 20 ) ;
   
   SshClientFPPanel(){
       setLayout( new CenterLayout(  ) ) ;
       Panel p = new Panel( new GridLayout(0,1) ) ;
       p.setBackground( Color.yellow ) ;
       p.add( new Label( "Dear User, the Host" , Label.CENTER  ) ) ;
       p.add( _hostLabel = new Label("",Label.CENTER ) ) ;
       _hostLabel.setForeground( Color.blue ) ;
       _hostLabel.setFont( _font ) ;
       p.add( new Label( "has sent us the Fingerprint",Label.CENTER  ) ) ;
       p.add( _fingerprintLabel = new Label( "0:0:0" , Label.CENTER ) ) ;
       p.add( new Label( "do you want to", Label.CENTER  ) ) ;
       Panel yesNo = new Panel( new GridLayout(1,2) ) ;
       
       yesNo.add( _acceptButton = new Button( "Accept it" ) ) ;
       _acceptButton.setBackground( Color.green ) ;
       yesNo.add( _rejectButton = new Button( "Reject it" ) ) ;
       _rejectButton.setBackground( Color.red ) ;
       _rejectButton.setForeground( Color.white ) ;
//       p.validateTree() ;
       p.add( yesNo ) ;
       p.doLayout();
   
       add( new dmg.cells.applets.spy.BorderPanel( p ) ) ;
       _acceptButton.addActionListener( this ) ;
       _rejectButton.addActionListener( this ) ;
   }
   public void setFingerPrint( String host , String fp ){
       _hostLabel.setText( host ) ;
       _fingerprintLabel.setText( fp ) ;
       System.out.println( "Enforcing layout" ) ;
       doLayout() ;
       System.out.println( "Layout done" ) ;
   }
   public void actionPerformed( ActionEvent event ){

      String command = event.getActionCommand() ;
      System.out.println( "Action : "+command ) ;
      Object obj = event.getSource() ;

      if( obj == _acceptButton ){
         informActionListeners( "accept" ) ;
      }else if( obj == _rejectButton ){
         informActionListeners( "reject" ) ;
      }
   }     


} 