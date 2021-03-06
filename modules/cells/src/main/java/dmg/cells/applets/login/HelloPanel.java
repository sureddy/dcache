package dmg.cells.applets.login ;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HelloPanel
       extends Panel
       implements ActionListener {

   private static final long serialVersionUID = -4656680180977452566L;
   private Component _picture      = new PictureCanvas() ;
   private Button    _setupButton  = new Button("Setup") ;
   private Button    _exitButton   = new Button("Exit" ) ;
   private Label     _messageLabel = new Label("") ;
   private Label     _title;
   private Color     _ourColor     = new Color( 10, 100 ,200 ) ;
   private Font _standardFont = new Font( "SansSerif" , 0 , 16 ) ;
   private Font _veryBigFont  = new Font( "SansSerif" , Font.BOLD | Font.ITALIC , 50 ) ;

   private LoginPanel  _loginPanel;
   private SetupPanel  _setupPanel;

   private int _b = 10 ;
   @Override
   public Insets getInsets(){ return new Insets( _b , _b ,_b , _b ) ; }
   @Override
   public void paint( Graphics g ){
      Dimension d = getSize() ;
      int h = _b / 2 ;
      g.setColor( _ourColor ) ;
      int [] xs = new int[4] ;
      int [] ys = new int[4] ;
      xs[0] = h           ; ys[0] = h ;
      xs[1] = d.width-h-1 ; ys[1] = h ;
      xs[2] = d.width-h-1 ; ys[2] = d.height - h - 1 ;
      xs[3] = h           ; ys[3] = d.height - h - 1 ;
      g.drawPolygon( xs , ys , xs.length  ) ;
   }
   private class PictureCanvas extends Canvas {
       private static final long serialVersionUID = -7791775854129610704L;

       public PictureCanvas(){
          repaint() ;
       }
       @Override
       public Dimension getMinimumSize(){
          return new Dimension(100,100) ;
       }
       @Override
       public Dimension getPreferredSize(){ return getMinimumSize() ; }
       @Override
       public void paint( Graphics g ){
          Dimension d = getSize() ;
//          System.out.println( "Painting " + d ) ;
          g.setColor(  _ourColor ) ;
          g.fillRect( 0 , 0 , d.width - 1 , d.height - 1 ) ;
          g.setColor( Color.red ) ;
          g.setFont( _veryBigFont ) ;
          g.drawString( "E" , 10 , d.height - 10 ) ;
          g.drawString( "G" , 50 , d.height - 50 ) ;
       }
   }
   @Override
   public Dimension getMinimumSize(){
      return new Dimension(500,200) ;
   }
   @Override
   public Dimension getPreferredSize(){ return getMinimumSize() ; }
   private class LoginPanel extends Panel implements ActionListener {
      private static final long serialVersionUID = -5700444988956707265L;
      private String    _password     = "" ;
      private TextField _loginText    = new TextField("") ;
      private TextField _passwordText = new TextField("") ;
      public LoginPanel(){
         KeyValueLayout kvl = new KeyValueLayout() ;
         kvl.setHgap( 10 ) ;
         kvl.setVgap( 10 ) ;
         setLayout( kvl ) ;
         Label login     = new Label( "Login" ) ;
         Label password  = new Label( "Password" ) ;

         login.setFont( _standardFont ) ;
         password.setFont( _standardFont ) ;


         _loginText.setBackground( Color.white ) ;
         _passwordText.setBackground( Color.white ) ;
         _passwordText.setEchoChar('*') ;
         _passwordText.addActionListener( this ) ;
         add( login ) ;
         add( _loginText ) ;
         add( password ) ;
         add( _passwordText ) ;

      }
      @Override
      public synchronized void actionPerformed( ActionEvent event ){
         _password = _passwordText.getText() ;
         _passwordText.setText("") ;
      }
      public void addActionListener( ActionListener al ){
         _passwordText.addActionListener( al ) ;

      }
      private String getUser(){ return _loginText.getText() ; }
      private String getPassword(){
          return _password ;
      }
      private void setUser( String user , boolean visible , boolean changable ){
         _loginText.setText(user) ;
      }
      private void setPassword( String password , boolean visible , boolean changable ){
         _passwordText.setText(password) ;
      }
      @Override
      public void setEnabled( boolean enable ){
         _loginText.setEnabled(enable) ;
         _passwordText.setEnabled(enable);
      }
   }
   private class SetupPanel extends Panel {
      private static final long serialVersionUID = 8087686727349989924L;
      private TextField _hostText = new TextField("") ;
      private TextField _portText = new TextField("") ;
      public SetupPanel(){
         setBackground( Color.green ) ;
         KeyValueLayout kvl = new KeyValueLayout() ;
         kvl.setHgap( 10 ) ;
         kvl.setVgap( 10 ) ;
         setLayout( kvl ) ;
         Label hostLabel = new Label( "Hostname" ) ;
         Label portLabel = new Label( "Portnumber" ) ;

         hostLabel.setFont( _standardFont ) ;
         portLabel.setFont( _standardFont ) ;


         _hostText.setBackground( Color.white ) ;
         _portText.setBackground( Color.white ) ;

         add( hostLabel ) ;
         add( _hostText ) ;
         add( portLabel ) ;
         add( _portText ) ;
      }
      private String getHost(){ return _hostText.getText() ; }
      private String getPort(){ return _portText.getText() ; }
      private void setHost( String hostname , boolean visible , boolean changable ){
         _hostText.setText(hostname) ;
         _hostText.setEditable(changable) ;
      }
      private void setPort( String port , boolean visible , boolean changable ){
         _portText.setText(port) ;
         _portText.setEditable(changable) ;
      }
      @Override
      public void setEnabled( boolean enable ){
         _portText.setEnabled(enable) ;
         _hostText.setEnabled(enable);
      }
   }
   public void setHost( String host , boolean visible , boolean changable ){
      _setupPanel.setHost( host , visible , changable ) ;
   }
   public void setPort( String port , boolean visible , boolean changable ){
      _setupPanel.setPort( port , visible , changable ) ;
   }
   public void setUser( String user , boolean visible , boolean changable ){
      _loginPanel.setUser( user , visible , changable ) ;
   }
   public String getUser(){
      return _loginPanel.getUser() ;
   }
   public String getHost(){
      return _setupPanel.getHost() ;
   }
   public String getPort(){
      return _setupPanel.getPort() ;
   }
   public String getPassword(){
      return _loginPanel.getPassword() ;
   }
   public void setText( String message ){
      if( message.equals("") ){
         _messageLabel.setText("") ;
      }else{
         _messageLabel.setForeground(Color.red) ;
         _messageLabel.setText(message);
      }
   }
   private ActionListener _actionListener;

   public void addActionListener( ActionListener listener ){
     _actionListener = listener ;
   }
   private void informActionListeners(String msg ){
      if( _actionListener != null ) {
          _actionListener.actionPerformed(
                  new ActionEvent(this, 0, msg));
      }
   }
   public void setTitle( String title ){}
   public void ok(){}
   public HelloPanel(String title ){
       _title = new Label( title,Label.CENTER) ;
       _title.setFont( new Font( "SansSerif" , Font.BOLD , 24 ) ) ;
       _title.setForeground( _ourColor ) ;
       BorderLayout masterLayout = new BorderLayout() ;
       masterLayout.setHgap(10) ;
       masterLayout.setVgap(10) ;
       setLayout( masterLayout ) ;

       add( _title , "North" ) ;

       add( _picture , "West" ) ;

       _loginPanel = new LoginPanel() ;
       _loginPanel.addActionListener( this ) ;

       _setupPanel = new SetupPanel() ;

       add( _loginPanel , "Center" ) ;

       BorderLayout bottomLayout = new BorderLayout() ;
       bottomLayout.setHgap(10) ;
       bottomLayout.setVgap(10) ;

       Panel bottomPanel = new Panel( bottomLayout ) ;

       _setupButton.setFont( _standardFont ) ;
       _setupButton.addActionListener( this ) ;
       _exitButton.setFont( _standardFont ) ;
       _exitButton.addActionListener( this ) ;
       bottomPanel.add( _setupButton , "West" ) ;
       bottomPanel.add( _exitButton  , "East" ) ;
       bottomPanel.add( _messageLabel , "Center" ) ;

       add( bottomPanel , "South") ;
   }
   @Override
   public void setEnabled( boolean enable ){
      _setupPanel.setEnabled(enable) ;
      _loginPanel.setEnabled(enable) ;
   }
   @Override
   public synchronized void actionPerformed( ActionEvent event ){
       String command = event.getActionCommand() ;
       Object obj     = event.getSource() ;
//       System.out.println( "Commamnd : "+command+" from "+obj.getClass().getName() ) ;
       if( obj == _exitButton ){
           informActionListeners("exit");
       }else if( obj == _setupButton ){
          if( command.equals(  "Setup" ) ){
             _setupButton.setLabel("Login") ;
             remove( _loginPanel ) ;
             _setupPanel.validate() ;
             add( _setupPanel , "Center" ) ;
             validate() ;
          }else{
             _setupButton.setLabel("Setup") ;
             remove( _setupPanel ) ;
             add( _loginPanel , "Center" ) ;
          }
          repaint() ;
       }else if( obj == _loginPanel._passwordText ){
          if( _loginPanel.getUser().equals("") ||
              _loginPanel.getPassword().equals("") ||
              _setupPanel.getHost().equals("") ||
              _setupPanel.getPort().equals("")       ){
              setText( "Not enough arguments" ) ;
          }else{
             setText("");
             informActionListeners( "go" ) ;
          }
       }
   }
}
