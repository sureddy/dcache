// $Id: CellDomainTree.java,v 1.3 2005-03-07 14:56:33 patrick Exp $

package dmg.cells.services.gui.realm ;

import javax.swing.BorderFactory;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import dmg.cells.applets.login.DomainConnection;
import dmg.cells.applets.login.DomainConnectionListener;
import dmg.cells.network.CellDomainNode;
import dmg.cells.nucleus.CellInfo;

public class CellDomainTree
       extends JTree  {

   private static final long serialVersionUID = 8632929106052533043L;
   private DefaultMutableTreeNode _root      = new DomainRootNode() ;
   private DefaultTreeModel       _treeModel = new DefaultTreeModel(_root) ;
   private DomainConnection _connection;
   private String _topoAddress = "topo@httpdDomain" ;
//   private String _topoAddress = "topo" ;

   public CellDomainTree( DomainConnection connection ){
      _connection = connection ;
      setModel( _treeModel ) ;
      collapseRow(0);
      addTreeExpansionListener( new DomainTreeExpansion() ) ;
      setBorder(
        BorderFactory.createCompoundBorder(
          BorderFactory.createEmptyBorder(10,10,10,10) ,
          BorderFactory.createCompoundBorder(
               BorderFactory.createTitledBorder(
                  null , "Cell Tree" , TitledBorder.LEFT , TitledBorder.TOP ) ,
                 BorderFactory.createEmptyBorder(10,10,10,10)   )
                                           )

      ) ;
   }
//   public Insets getInsets(){ return new Insets(30,30,30,30) ; }

   private class CellTreeNode
           extends DefaultMutableTreeNode
           implements  DomainConnectionListener {

      private static final long serialVersionUID = -2869631713894791778L;
      private boolean _isLeaf;
      private boolean _valuesSet;
      private String  _name;

      public CellTreeNode( String name , boolean isLeaf ){
         _name   = name ;
         _isLeaf = isLeaf ;
         setUserObject(this);
      }
      @Override
      public void domainAnswerArrived( Object obj , int subid ){
//         System.out.println( "Answer ("+subid+") : "+obj.toString() ) ;
      }
      public void expanded(){
         setEnabled(false);
         System.out.println("Expanded : "+this.toString());
      }
      public void done(){ setEnabled(true);}
      @Override
      public boolean getAllowsChildren(){ return ! _isLeaf ; }
      @Override
      public boolean isLeaf(){ return _isLeaf ; }
      public String toString(){ return _name ; }
      public boolean isValueSet(){ return _valuesSet ; }
      public void setValueSet( boolean valueSet ){ _valuesSet = valueSet ; }
      public void addDelayed( final CellTreeNode [] nodes ){
         final CellTreeNode self = this ;
         new Thread(
            new Runnable(){
              @Override
              public void run(){
                 try{
                    Thread.sleep(5000) ;
                 }catch(InterruptedException ee){}
                 SwingUtilities.invokeLater(
                   new Runnable(){
                      @Override
                      public void run(){
                          for (CellTreeNode node : nodes) {
                              _treeModel.insertNodeInto(node, self, 0);
                          }
                        setValueSet(true) ;
                        done();
                      }
                   }
                 );
              }
            }
         ).start() ;
      }
   }
   private class CellDomainTreeNode extends CellTreeNode {
      private static final long serialVersionUID = -4919824203352216297L;
      private CellDomainNode _node;
      public CellDomainTreeNode( CellDomainNode node ){
         super( node.getName() , false ) ;
         _node = node ;
      }
      @Override
      public void expanded(){
         if( ! isValueSet() ){
            super.expanded();
            _treeModel.insertNodeInto(
               new CellContainerNode("CELLS",_node ) , this , 0 ) ;
            _treeModel.insertNodeInto(
               new ContextContainerNode("CONTEXT",_node) , this , 0 ) ;
            _treeModel.insertNodeInto(
               new RouterNode("ROUTES" , _node ) , this , 0 ) ;
            done() ;
            setValueSet(true);
         }
      }
   }
   public class CellNode extends CellTreeNode {
      private static final long serialVersionUID = 8523105175296824521L;
      private CellInfo _cellInfo;
      private String   _address;
      private CellNode( String address , CellInfo info  ){
         super( info.getCellName() , true ) ;
         _cellInfo = info ;
         _address = address ;
      }
      public String getAddress(){ return _address ; }
      public CellInfo getCellInfo(){ return _cellInfo ; }
   }
   private class RouterNode extends CellTreeNode {
      private static final long serialVersionUID = -3709341083761440849L;

      public RouterNode(String name , CellDomainNode node ){
         super( name , true ) ;
      }
   }
   private class ContextNode extends CellTreeNode {
      private static final long serialVersionUID = 4917133149526146830L;

      public ContextNode(String name  ){
         super( name , true ) ;
      }
   }
   private class ContextContainerNode extends CellTreeNode {
      private static final long serialVersionUID = 4649794381123797011L;
      private String      [] _context;
      private CellDomainNode _node;
      public ContextContainerNode(String name , CellDomainNode node){
         super( name , false ) ;
         _node = node ;
      }
      @Override
      public void expanded(){
         if( ! isValueSet() ){
            super.expanded();
            try{
               _connection.sendObject(_node.getAddress(),"getcontext",this,0) ;
            }catch(Exception ee){
               ee.printStackTrace() ;
               done() ;
            }
         }
      }
      @Override
      public void domainAnswerArrived( Object obj , int subid ){
         if( ! ( obj instanceof String [] ) ) {
             return;
         }
         final CellTreeNode self = this ;
         _context = (String [] )obj ;
         SwingUtilities.invokeLater(
            new Runnable(){
               @Override
               public void run(){
                  for( int i = 0 ; i < _context.length ; i++ ){
                     _treeModel.insertNodeInto(
                          new ContextNode( _context[i] ) ,
                          self ,
                          i ) ;
                  }
                  setValueSet(true);
                  done() ;

               }
            }
         ) ;
      }
   }
   private class CellContainerNode extends CellTreeNode {
      private static final long serialVersionUID = 3794317629384966493L;
      private CellDomainNode _node;
      private CellInfo   []  _cellInfo;
      public CellContainerNode(String name , CellDomainNode node ){
         super( name , false ) ;
         _node = node ;
      }
      @Override
      public void expanded(){
         if( ! isValueSet() ){
            super.expanded();
            try{
               _connection.sendObject(_node.getAddress(),"getcellinfos",this,0) ;
            }catch(Exception ee){
               ee.printStackTrace() ;
               done() ;
            }
         }
      }
      @Override
      public void domainAnswerArrived( Object obj , int subid ){
         System.out.println("CellContainerNode : "+obj.getClass().getName() ) ;
         if( ! ( obj instanceof CellInfo [] ) ) {
             return;
         }
         final CellTreeNode self = this ;
         _cellInfo = (CellInfo [] )obj ;
         SwingUtilities.invokeLater(
            new Runnable(){
               @Override
               public void run(){
                  int systemIndex = -1 ;
                  for( int i = 0 ; i < _cellInfo.length ; i++ ){
                     if( _cellInfo[i].getCellName().equals("System") ){
                        systemIndex = i ;
                     }else{
                        _treeModel.insertNodeInto(
                             new CellNode( _node.getAddress(),_cellInfo[i] ) ,
                             self ,
                             0 ) ;
                     }
                  }
                  _treeModel.insertNodeInto(
                       new CellNode( _node.getAddress(),_cellInfo[systemIndex] ) ,
                       self ,
                       0 ) ;

                  setValueSet(true);
                  done() ;

               }
            }
         ) ;
      }
   }

   private class DomainRootNode extends CellTreeNode {
      private static final long serialVersionUID = -2017147296172009929L;
      private CellDomainNode [] _nodes;
      public DomainRootNode(){
         super("Realm",false);
      }
      @Override
      public void expanded(){
         if( ! isValueSet() ){
            super.expanded();
            try{
               _connection.sendObject( _topoAddress,"gettopomap",this,0) ;
            }catch(Exception ee){
               ee.printStackTrace() ;
               done() ;
            }
         }
      }
      @Override
      public void domainAnswerArrived( Object obj , int subid ){
         if( ! ( obj instanceof CellDomainNode [] ) ) {
             return;
         }
         final CellTreeNode self = this ;
         _nodes = (CellDomainNode [] )obj ;
         SwingUtilities.invokeLater(
            new Runnable(){
               @Override
               public void run(){
                  for( int i = 0 ; i < _nodes.length ; i++ ){
                     _treeModel.insertNodeInto(
                          new CellDomainTreeNode( _nodes[i] ) ,
                          self ,
                          i ) ;
                  }
                  setValueSet(true);
                  done() ;

               }
            }
         ) ;
      }
   }
   private class DomainTreeExpansion implements TreeExpansionListener {

      @Override
      public void treeExpanded( TreeExpansionEvent event ){

        CellTreeNode node = (CellTreeNode)event.getPath().getLastPathComponent() ;
        node.expanded() ;
      }
      @Override
      public void treeCollapsed( TreeExpansionEvent event ){
        System.out.println( "Collapsed event : "+event.getPath() ) ;
      }
   }
}
