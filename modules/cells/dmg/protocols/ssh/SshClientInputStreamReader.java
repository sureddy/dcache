package dmg.protocols.ssh ;

import java.io.* ;
 
public class SshClientInputStreamReader extends InputStreamReader {

  SshOutputStreamWriter _output ;
  
  public SshClientInputStreamReader( InputStream input , OutputStream output ){
     super( input  ) ;
 //    System.out.println( "InputStreamReader created" ) ;
  }
  
  
  public int read( char [] cbuf , int off , int len )
         throws IOException {
     
    int rc;
    while( true ){
       rc = super.read( cbuf , off, 1 ) ;
       if( rc <= 0 )return rc ;
       
       
       if( cbuf[off] == 10 ){ 
           cbuf[off] = '\n' ;
           return 1 ;
       }else if( cbuf[off] == 13 ) continue ;
       else{
          return 1 ;
       }
       
    }
  }

}