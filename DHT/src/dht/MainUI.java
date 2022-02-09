package dht;

import java.awt.Color;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;


public class MainUI {
	static {
		System.setProperty("java.util.logging.SimpleFormatter.format",
				"[%1$tF %1$tT][%4$-7s] [%5$s] [%2$-7s] %n");

		
	}

	static final Logger LOGGER = Logger.getLogger(MainUI.class.getName());
	private JFrame frmDht;
	private JTextField textField;
	private JTable table;	
	private JLabel memberL;
	DefaultTableCellRenderer centerRenderer;
	//DHTManager dht;
	zkMember zkm;
	TableColumnModel columns;
	private JTextField key;
	private JTextField value;
	private boolean localZK;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainUI window = new MainUI(args);
					window.frmDht.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	public void configureLogger() {
		ConsoleHandler handler;
		handler = new ConsoleHandler(); 
		handler.setLevel(Level.FINE); 
		LOGGER.addHandler(handler); 
		LOGGER.setLevel(Level.FINE); 
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	public void setLabel(int pos, String localMember, int nServers) {
		memberL.setText("Local server: " + localMember + " [" + pos + "/" + nServers + "]");
	}
	
	private void initMembers() {
		
			zkm.put(new DHT_Map("Angel", 1));
		
			zkm.put(new DHT_Map("Bernardo", 2));
		
			zkm.put(new DHT_Map("Carlos", 3));
		
			zkm.put(new DHT_Map("Daniel", 4));
		
			zkm.put(new DHT_Map("Eugenio", 5));
		
			zkm.put(new DHT_Map("Zamorano", 6));
		
	}

	/**
	 * Create the application.
	 */
	public MainUI(String[] args) {
		configureLogger();
		if(args.length == 0)	this.localZK = true;
		initialize();
		
	}
	
public void updateTable() {
		LOGGER.fine("Updating table");
		
		HashMap<Integer, DHTUserInterface> dhttables = zkm.getDHTTables();
		
		int nCols = dhttables.size();
		
		String[] colNames = new String[nCols];
		
		for(int i=0; i < nCols; i++) {
			colNames[i] = "Table " + i;
			
		}
		
		int largestTable = 0;
		for(int i=0; i < dhttables.size();i++)
		{
			if(dhttables.get(i).values().size() > largestTable)
				largestTable = dhttables.get(i).values().size();
		}
		
		
		table.setModel(new DefaultTableModel(
				new Object[largestTable][nCols],
				colNames
			));
		
		columns = table.getColumnModel();
		
		for(int i=0; i < dhttables.size();i++)
		{
			columns.getColumn(i).setCellRenderer(centerRenderer);
			int j=0;
			ArrayList<Integer> values = dhttables.get(i).values();
			Collections.sort(values);
			for(Integer value: values) {		    	
		    	table.setValueAt(value, j, i);
		    	j++;
		    }
		}		
	}


	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmDht = new JFrame();
		frmDht.setTitle("DHT");
		frmDht.setResizable(false);
		frmDht.setBounds(100, 100, 450, 376);
		frmDht.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmDht.getContentPane().setLayout(null);
		
		centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);	
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(12, 12, 426, 317);
		frmDht.getContentPane().add(tabbedPane);
		tabbedPane.setBorder(null);
		
		JPanel operationsPanel = new JPanel();
		tabbedPane.addTab("Operations", null, operationsPanel, null);
		operationsPanel.setBorder(null);
		operationsPanel.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(0, 2, 420, 20);
		operationsPanel.add(panel);
		
		memberL = new JLabel("Local server:");
		panel.add(memberL);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(6, 30, 410, 150);
		operationsPanel.add(scrollPane);
		
		table = new JTable();
		table.setBounds(0, 30, 500, 500);
		//operationsPanel.add(table);
		scrollPane.setViewportView(table);
		scrollPane.setVisible(true);
		table.setFillsViewportHeight(true);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setBounds(6, 200, 164, 24);
		operationsPanel.add(comboBox);
		
		String[] operations = {"PUT", "GET", "REMOVE", "CONTAINS KEY", "INITIALIZE"};
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"- Select an Option -", "PUT", "GET", "REMOVE", "CONTAINS KEY", "INITIALIZE"}));
		
		
		JButton process = new JButton("Process");
		process.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sel = comboBox.getSelectedItem().toString();
				
				if(sel == "- Select an Option -") 
					JOptionPane.showMessageDialog(null, "Please, select an option", "InfoBox: DHT" , JOptionPane.INFORMATION_MESSAGE);
				else if(sel == "PUT") {
					if(key.getText() == "" || value.getText() == "") {
						JOptionPane.showMessageDialog(null, "Key and value cannot be empty", "InfoBox: DHT" , JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					int val=0;
					try {
						val = Integer.parseInt(value.getText());
						if(!(key.getText() instanceof String)) {
							throw new Exception();
						}
					}
					catch (Exception ex) {
						JOptionPane.showMessageDialog(null, "Key must be a string and value must be an integer", "InfoBox: DHT" , JOptionPane.INFORMATION_MESSAGE);
						return;
					}						
					
					zkm.put(new DHT_Map(key.getText(), val));						
				}
				else if (sel == "GET") {
					if(key.getText() == "" || !(key.getText() instanceof String)) {
						JOptionPane.showMessageDialog(null, "Key must be a non-empty string", "InfoBox: DHT" , JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					
					Integer val  = zkm.get(key.getText());
					if(val != null)
						value.setText(val.toString());
					else
						JOptionPane.showMessageDialog(null, "Key '" + key.getText() + "' does not exist", "InfoBox: DHT" , JOptionPane.INFORMATION_MESSAGE);						
				}
				else if (sel == "REMOVE") {
					if(key.getText() == "" || !(key.getText() instanceof String)) {
						JOptionPane.showMessageDialog(null, "Key must be a non-empty string", "InfoBox: DHT" , JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					
					Integer val  = zkm.remove(key.getText());
					if(val != null) {
						value.setText(val.toString());										
					}
					else
						JOptionPane.showMessageDialog(null, "Key '" + key.getText() + "' does not exist", "InfoBox: DHT" , JOptionPane.INFORMATION_MESSAGE);
					
				}
				else if (sel == "CONTAINS KEY") {
					if(key.getText() == "" || !(key.getText() instanceof String)) {
						JOptionPane.showMessageDialog(null, "Key must be a non-empty string", "InfoBox: DHT" , JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					
					if (zkm.containsKey(key.getText())) 
						JOptionPane.showMessageDialog(null, "Key '" + key.getText() + "' exists", "InfoBox: DHT" , JOptionPane.INFORMATION_MESSAGE);
					else
						JOptionPane.showMessageDialog(null, "Key '" + key.getText() + "' does not exist", "InfoBox: DHT" , JOptionPane.INFORMATION_MESSAGE);						
				}
				else {
					initMembers();
					updateTable();
				}	
				comboBox.setSelectedIndex(0);
			}
		});
		process.setBounds(6, 245, 164, 24);
		operationsPanel.add(process);
		
		JLabel lblKey = new JLabel("Key");
		lblKey.setBounds(230, 205, 70, 15);
		operationsPanel.add(lblKey);
		
		JLabel lblValue = new JLabel("Value");
		lblValue.setBounds(230, 250, 70, 15);
		operationsPanel.add(lblValue);
		
		key = new JTextField();
		key.setBounds(276, 200, 140, 24);
		operationsPanel.add(key);
		key.setColumns(10);
		
		value = new JTextField();
		
		
		value.setBounds(276, 245, 140, 24);
		operationsPanel.add(value);
		value.setColumns(10);
		
		JPanel configPanel = new JPanel();
		tabbedPane.addTab("Configuration", null, configPanel, null);
		configPanel.setLayout(null);
		
		JLabel lblZookeeper = new JLabel("ZooKeeper");
		lblZookeeper.setBounds(12, 12, 79, 15);
		configPanel.add(lblZookeeper);
		
		textField = new JTextField();
		textField.setText("127.0.0.1:2181");
		textField.setBounds(109, 10, 300, 19);
		configPanel.add(textField);
		textField.setColumns(10);
		
		JButton configOK = new JButton("Start DHT");
		configOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//dht        = new DHTManager(MainUI.this);
				zkm = new zkMember(MainUI.this, textField.getText());
				tabbedPane.setSelectedIndex(0);
				tabbedPane.setEnabledAt(1, false);
			}
		});
		configOK.setBounds(292, 49, 117, 25);
		configPanel.add(configOK);
		
		if(!localZK) {
			tabbedPane.setSelectedIndex(1);
			tabbedPane.setEnabledAt(0, false);
		}
		else {
			tabbedPane.setEnabledAt(1, false);
			//dht        = new DHTManager(this);
			zkm = new zkMember(this, textField.getText());
		}
		
		
	}
}
