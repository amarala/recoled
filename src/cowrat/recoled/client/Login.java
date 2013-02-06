package cowrat.recoled.client;

import javax.swing.* ;
import javax.swing.border.* ;
import java.awt.* ;
import cowrat.recoled.shared.MyColors;
import cowrat.recoled.shared.MyImageIcon;

public class Login extends JFrame {
	
    private JLabel lblName, lblColor ;
    private JTextField txtName ;
    private JComboBox cmbColor ;

    private JPanel clients ;
    
    public JButton cmdLogin ;
    
    public String getName() { return txtName.getText() ;}
    public int getColor() { return cmbColor.getSelectedIndex() + 1 ;}

    public void addClient(String name, int colorCode) {



	JLabel title = new JLabel(name, JLabel.CENTER) ;
	title.setBackground(MyColors.getMainColor(colorCode)) ;
	title.setForeground(Color.WHITE) ;
	IncompleteLineBorder b = new IncompleteLineBorder(Color.BLACK) ;
	b.setExcludeBottom(true) ;
	title.setBorder(b) ;
	title.setOpaque(true) ;
	title.setMaximumSize(new Dimension(100, 20)) ;
	title.setMinimumSize(new Dimension(100, 20)) ;
	title.setPreferredSize(new Dimension(100, 20)) ;

	JLabel image = new JLabel(new MyImageIcon("images/client_waiting.gif")) ;
	image.setBackground(MyColors.getLightColor(colorCode)) ;
	image.setOpaque(true) ;
	image.setBorder(new LineBorder(Color.BLACK)) ;
	image.setMaximumSize(new Dimension(100, 95)) ;
	image.setMinimumSize(new Dimension(100, 95)) ;
	image.setPreferredSize(new Dimension(100, 95)) ;
	

	JPanel client = new JPanel(new BorderLayout()) ;
	client.add(title, BorderLayout.NORTH) ;
	client.add(image, BorderLayout.CENTER) ;
	client.setMaximumSize(new Dimension(100, 115)) ;

	if (clients.getComponentCount() > 0)
	    clients.add(Box.createHorizontalStrut(5)) ;

	clients.add(client) ;
	validate() ;
    }

    public void showUsedNameMessage() {
	JOptionPane.showMessageDialog(this, "This name is already in use", 
				      "Invalid Name", JOptionPane.ERROR_MESSAGE) ;
    }

    public void showUsedColorMessage() {
	JOptionPane.showMessageDialog(this, "This color is already in use", 
				      "Invalid Color", JOptionPane.ERROR_MESSAGE) ;
    }
    
    public Login() {
		
	super() ;
	setTitle("SharedEditor: Login") ;

	clients = new JPanel() ;
	clients.setLayout(new BoxLayout(clients, BoxLayout.X_AXIS)) ;
	clients.setBorder(new EmptyBorder(5,5,5,5)) ;

	JScrollPane clientScroll = new JScrollPane(clients) ;
	clientScroll.setPreferredSize(new Dimension (600, 130)) ;
	clientScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER) ;
		
	lblName = new JLabel("Name:") ;
	txtName = new JTextField() ;
	txtName.setMaximumSize(new Dimension(500, 22)) ;
	Box boxName = new Box(BoxLayout.X_AXIS) ;
	boxName.add(lblName) ;
	boxName.add(Box.createHorizontalStrut(10)) ;
	boxName.add(txtName) ;
		
	lblColor = new JLabel ("Color:") ;
	cmbColor = new JComboBox() ;
	cmbColor.setMaximumSize(new Dimension(500, 22)) ;

	for (int i=1 ; i < 7 ; i++)
	    cmbColor.addItem(MyColors.getName(i)) ;


	Box boxColor = new Box(BoxLayout.X_AXIS) ;
	boxColor.add(lblColor) ;
	boxColor.add(Box.createHorizontalStrut(10)) ;
	boxColor.add(cmbColor) ;
		
	cmdLogin = new JButton("Login") ;
	cmdLogin.setMaximumSize(new Dimension(500, 26)) ;
	Box boxLogin = new Box(BoxLayout.X_AXIS) ;
	boxLogin.add(Box.createGlue()) ;
	boxLogin.add(cmdLogin) ;
	boxLogin.add(Box.createGlue()) ;
		
	JPanel all = new JPanel() ;
	all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS)) ;
	all.setBorder(new EmptyBorder(10, 10, 10, 10)) ;
	all.add(Box.createGlue()) ;
	all.add(clientScroll) ;
	all.add(Box.createGlue()) ;
	all.add(boxName) ;
	all.add(Box.createVerticalStrut(10)) ;
	all.add(boxColor) ;
	all.add(Box.createGlue()) ;
	all.add(boxLogin) ;
		
	getContentPane().add(all, BorderLayout.CENTER) ;
    }
}





