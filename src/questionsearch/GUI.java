package questionsearch;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.InputMismatchException;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/**
 * This class creates the FedexSimulator JFrame and is the main GUI class.
 */

public class GUI extends JFrame
{
  /**
   * Constructor. This creates the JFrame that controls the simulation.
   */
 
  static DefaultListModel< String > ParcelDefaultListModel;
  DefaultListModel< String > VehicleDefaultListModel;
  static JList< String > ParcelList;
  JList< String > VehicleList;
  JButton StartSimulation;
  JButton AdvanceSimulation;
  JButton EndSimulation;
  JButton ClearVehicles;
  JButton ClearParcelList;
  JButton ClearOutput;
  JButton Reset;
  JPanel framePanel;
  JPanel leftPanel;
  JPanel topleftPanel;
  JPanel botleftPanel;
  JPanel Buttons;
  JPanel VehiclesPanel;
  JPanel ParcelPanel;
  JPanel OutputPanel;
  JPanel bottomPanel;
  static JTextArea output;
  JLabel Output;
  JLabel VehicleType;
  JLabel VehicleListLabel;
  JLabel ParcelType;
  JLabel ParcelListLabel;
  JLabel AdvanceNextTickAmount;
  JLabel DumpStatsAfterNextAdvance;
  JComboBox VehicleListBox;
  static JComboBox ParcelListBox;
  JTextField TickAmount;
  JCheckBox DumpStats;
  JMenu fileMenu;
  JMenu editMenu;
  ActionListener Action;
  boolean SimulationRunning;
  
  public GUI()
  {
    
    super("Piazza QA");
    framePanel = new JPanel();
    leftPanel = new JPanel();
    topleftPanel = new JPanel();
    botleftPanel = new JPanel();
    framePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(framePanel);
    framePanel.setLayout(new BoxLayout(framePanel, BoxLayout.X_AXIS));
    leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
    topleftPanel.add(createVehiclesPanel());
    botleftPanel.add(createButtonsPanel());
    leftPanel.add(topleftPanel);
    leftPanel.add(botleftPanel);
    framePanel.add(leftPanel);
    JPanel outPutPanel = createOutputPanel();
    framePanel.add(outPutPanel);
    setJMenuBar(makeMenuBar());
    pack();
    ///remember to remove cancel button from confirm dialogs
  }

  public static void main(String [] args){
	  GUI win = new GUI();
	  win.setSize(1000,500);
	  win.setVisible(true);
  }
    
  String readToWrite(String s){
    System.out.println(s);
    String[] splitS = s.split(" ");
    return splitS[0] + "_" + splitS[1];  
  }
  
  String writeToRead(String s){
    String[] splitS = s.split("_");
    return splitS[0] + " " + splitS[1];
  }
  
  JPanel createButtonsPanel(){    
    SimulationRunning = false;
    Buttons = new JPanel();
    Buttons.setLayout(new BoxLayout(Buttons, BoxLayout.Y_AXIS));
    JPanel topButtons = new JPanel();
    topButtons.setLayout(new BoxLayout(topButtons, BoxLayout.X_AXIS));
    JPanel botButtons = new JPanel();
    botButtons.setLayout(new BoxLayout(botButtons, BoxLayout.X_AXIS));
    JPanel tickInfo = new JPanel();
    tickInfo.setLayout(new BoxLayout(tickInfo, BoxLayout.X_AXIS));
    JPanel dumpStats = new JPanel();
    dumpStats.setLayout(new BoxLayout(dumpStats, BoxLayout.X_AXIS));
    
    StartSimulation = new JButton("Start Simulation"); 
    StartSimulation.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e)
        {
        }
    });
    
    topButtons.add(StartSimulation);
    AdvanceSimulation = new JButton("Advance Simulation");
    AdvanceSimulation.setEnabled(false);
    AdvanceSimulation.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      {
      }
  });
    
    topButtons.add(AdvanceSimulation);
    EndSimulation = new JButton("End Simulation");
    EndSimulation.setEnabled(false);
    EndSimulation.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      {
      }
  });
    
    topButtons.add(EndSimulation);
    ClearVehicles = new JButton("Clear Vehicles");
    botButtons.add(ClearVehicles);
    ClearVehicles.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      {
      }
     });
    
    ClearParcelList = new JButton("Clear Parcel List");
    botButtons.add(ClearParcelList);
    ClearParcelList.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      {
      }
     });
    
    ClearOutput = new JButton("Clear Output");
    botButtons.add(ClearOutput);
    ClearOutput.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      {
      }
  });
   
    Reset = new JButton("Reset");
    botButtons.add(Reset);
    Reset.setEnabled(false);
    Reset.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      {
      }
  });
  
    AdvanceNextTickAmount = new JLabel("Advance Next Tick Amount:");
    TickAmount = new JTextField("1");
    tickInfo.add(AdvanceNextTickAmount);
    tickInfo.add(TickAmount);
    
    Buttons.add(topButtons);
    Buttons.add(botButtons);
    Buttons.add(tickInfo);
    Buttons.add(dumpStats);
    
    return Buttons;
  }
  
  JMenuBar makeMenuBar(){
    JMenuBar menuBar = new JMenuBar();
    fileMenu = new JMenu("File");
    
    JMenuItem LoadVehicles = new JMenuItem("Load vehicles");
    LoadVehicles.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      { 
      }
    });
    
    JMenuItem LoadParcels = new JMenuItem("Load parcels");
    LoadParcels.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      { 
      }
    });
    
    JMenuItem SaveOutput = new JMenuItem("Save Output");
    SaveOutput.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      { 
      }
    });
    
    JMenuItem SaveVehicles = new JMenuItem("Save vehicle list");
    SaveVehicles.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      { 
      }
    });

    JMenuItem SaveParcels = new JMenuItem("Save parcel list");
    SaveParcels.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      { 
      }
    });
 
    JMenuItem ExitProgram = new JMenuItem("Exit Program");
    ExitProgram.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      { 
        dispose();
      }
    });
    
    fileMenu.add(LoadVehicles);
    fileMenu.add(LoadParcels);
    fileMenu.add(SaveOutput);
    fileMenu.add(SaveVehicles);
    fileMenu.add(SaveParcels);
    fileMenu.add(ExitProgram);
    menuBar.add(fileMenu);
    
  //edit menu part
    editMenu = new JMenu("Edit");
    JMenuItem AddVehicle = new JMenuItem("Add Vehicle");
    AddVehicle.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      { 
        final JDialog addVehicleStuff = new JDialog(new JDialog(),"Add Vehicle");
        addVehicleStuff.setSize(510,110);
        addVehicleStuff.setResizable(false);
        addVehicleStuff.setLayout(new GridLayout(3,1));
        JPanel VehicleType = new JPanel(new FlowLayout()); 
        JLabel VehicleTypeLabel = new JLabel("Vehicle Type: ");
        VehicleType.add(VehicleTypeLabel);
        final JComboBox VehicleListBox = new JComboBox(); 
        VehicleListBox.addItem("Delivery Truck");
        VehicleListBox.addItem("Freight Truck");
        VehicleListBox.addItem("Cargo Plane");
        VehicleType.add(VehicleListBox);
        JPanel VehicleInfo = new JPanel(new GridLayout(1,4)); 
        JLabel VehicleStartXLabel = new JLabel("Starting X Position: ");
        VehicleInfo.add(VehicleStartXLabel);
        final JTextField VehicleStartX = new JTextField();
        VehicleStartX.setSize(100, 30);
        VehicleInfo.add(VehicleStartX);
        JLabel VehicleStartYLabel = new JLabel("Starting Y Position: ");
        VehicleInfo.add(VehicleStartYLabel);
        final JTextField VehicleStartY = new JTextField();
        VehicleInfo.add(VehicleStartY);
        VehicleStartY.setSize(100, 30);
        addVehicleStuff.add(VehicleType,BorderLayout.NORTH);
        addVehicleStuff.add(VehicleInfo,BorderLayout.CENTER);
        addVehicleStuff.setVisible(true);
      
        JPanel Confirmation = new JPanel(new FlowLayout()); 
        JButton Okay = new JButton("Okay");
        Okay.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e)
          {  
          }
        });
        Confirmation.add(Okay);
        
        JButton Cancel = new JButton("Cancel");
        Cancel.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e)
          { 
            addVehicleStuff.setVisible(false);
          }
        });
        Confirmation.add(Cancel);

        addVehicleStuff.add(VehicleType);
        addVehicleStuff.add(VehicleInfo);
        addVehicleStuff.add(Confirmation);
      }
    });

    JMenuItem AddParcel = new JMenuItem("Add Parcel");
    AddParcel.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      { 
        final JDialog addParcelStuff = new JDialog();
        addParcelStuff.setVisible(true);
        addParcelStuff.setSize(510,150);
        addParcelStuff.setResizable(false);
        addParcelStuff.setLayout(new GridLayout(4,1));
        
        JPanel ParcelType = new JPanel(new FlowLayout()); 
        JLabel ParcelTypeLabel = new JLabel("Parcel Type: ");
        ParcelType.add(ParcelTypeLabel);
        final JComboBox  ParcelListBox = new JComboBox();
        ParcelListBox.addItem("Large Parcel");
        ParcelListBox.addItem("Medium Parcel");
        ParcelListBox.addItem("Small Parcel");
        ParcelType.add(ParcelListBox);
        
        JPanel ParcelInfo1 = new JPanel(new GridLayout(1,4)); 
        JLabel ParcelStartXLabel = new JLabel("Starting X Position: ");
        ParcelInfo1.add(ParcelStartXLabel);
        final JTextField ParcelStartX = new JTextField();
        ParcelInfo1.add(ParcelStartX);
        JLabel ParcelStartYLabel = new JLabel("Starting Y Position: ");
        ParcelInfo1.add(ParcelStartYLabel);
        final JTextField ParcelStartY = new JTextField();
        ParcelInfo1.add(ParcelStartY);
       
        JPanel ParcelInfo2 = new JPanel(new GridLayout(1,4)); 
        JLabel ParcelEndXLabel = new JLabel("End X Position: ");
        ParcelInfo2.add(ParcelEndXLabel);
        final JTextField ParcelEndX = new JTextField();
        ParcelInfo2.add(ParcelEndX);
        JLabel ParcelEndYLabel = new JLabel("End Y Position: ");
        ParcelInfo2.add(ParcelEndYLabel);
        final JTextField ParcelEndY = new JTextField();
        ParcelInfo2.add(ParcelEndY);
      
        JPanel Confirmation = new JPanel(new FlowLayout()); 
        JButton Okay = new JButton("Okay");
        Confirmation.add(Okay);
        Okay.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e)
          { 
          }//for if
        });
        Confirmation.add(Okay);
        JButton Cancel = new JButton("Cancel");
        Cancel.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e)
          { 
            addParcelStuff.setVisible(false);
          }
        });
        Confirmation.add(Cancel);
        
        addParcelStuff.add(ParcelType);
        addParcelStuff.add(ParcelInfo1);
        addParcelStuff.add(ParcelInfo2);
        addParcelStuff.add(Confirmation);
          }
        });
    
    JMenuItem RerouteParcel = new JMenuItem("Reroute Parcel");
    RerouteParcel.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      { 
        final JDialog reRouteParcelStuff = new JDialog(new JDialog(),"Reroute Parcel");
        reRouteParcelStuff.setLayout(new GridLayout(3,1));
        reRouteParcelStuff.setVisible(true);
        reRouteParcelStuff.setResizable(false);
        reRouteParcelStuff.setSize(200,115);
        JPanel ParcelType = new JPanel(new GridLayout(1,2)); 
        JLabel ParcelIDLabel = new JLabel("Parcel ID: ");
        ParcelType.add(ParcelIDLabel);
        final JTextField ParcelID = new JTextField();
        ParcelType.add(ParcelID);  
        JPanel ParcelInfo = new JPanel(new GridLayout(1,2)); 
        JLabel ParcelXLabel = new JLabel("New X");
        ParcelInfo.add(ParcelXLabel);
        final JTextField ParcelX = new JTextField();
        ParcelInfo.add(ParcelX);
        JLabel ParcelYLabel = new JLabel("New Y");
        ParcelInfo.add(ParcelYLabel);
        final JTextField ParcelY = new JTextField();
        ParcelInfo.add(ParcelY);
        
        JPanel Confirmation = new JPanel(new FlowLayout()); 
        JButton Okay = new JButton("Okay");
        Confirmation.add(Okay);
        Okay.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e)
          { 
          }
        });
        
        JButton Cancel = new JButton("Cancel");
        Confirmation.add(Cancel);
        Cancel.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e)
          { 
            reRouteParcelStuff.setVisible(false);
          }
        });
        reRouteParcelStuff.add(ParcelType);
        reRouteParcelStuff.add(ParcelInfo);
        reRouteParcelStuff.add(Confirmation);
      }
    });
    
    JMenuItem DeleteVehicle = new JMenuItem("Delete Vehicle");
    DeleteVehicle.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      { 
    final JDialog DeleteVehicle = new JDialog(new JDialog(),"Delete Vehicle");
    DeleteVehicle.setLayout(new GridLayout(2,1));
    DeleteVehicle.setVisible(true);
    DeleteVehicle.setSize(360,90);
    DeleteVehicle.setResizable(false);
    JPanel VehicleToDelete = new JPanel(new GridLayout(1,2)); 
    JLabel VehicleToDeleteLabel = new JLabel("Please Enter the Vehicle ID: ");
    VehicleToDelete.add(VehicleToDeleteLabel);
    final JTextField VehicleID = new JTextField();
    VehicleToDelete.add(VehicleID);  
    JPanel Confirmation = new JPanel(new FlowLayout()); 
    JButton Delete = new JButton("Delete");
    Confirmation.add(Delete);
    Delete.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      { 
      }
    });
    JButton Cancel = new JButton("Cancel");
    Cancel.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      { 
        DeleteVehicle.setVisible(false);
        }
    });
    Confirmation.add(Cancel);
    DeleteVehicle.add(VehicleToDelete,BorderLayout.NORTH);
    DeleteVehicle.add(Confirmation,BorderLayout.CENTER); 
      }
    });
 
    JMenuItem DeleteParcel = new JMenuItem("Delete Parcel");
    DeleteParcel.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      { 
      }
     });
   
    editMenu.add(AddVehicle);
    editMenu.add(AddParcel);
    editMenu.add(RerouteParcel);
    editMenu.add(DeleteVehicle);
    editMenu.add(DeleteParcel);
    menuBar.add(editMenu);
    return menuBar;
    
  }
  
  JPanel createVehiclesPanel(){
    //adding vehicles stuff that I need
    VehiclesPanel = new JPanel();
    VehiclesPanel.setLayout(new BoxLayout(VehiclesPanel, BoxLayout.Y_AXIS));
    VehicleType = new JLabel("Show Selected Vehicle Type");
    VehiclesPanel.add(VehicleType);
    VehicleListBox = new JComboBox();
    VehicleListBox.addItem("All Vehicles");
    VehicleListBox.addItem("Delivery Truck");
    VehicleListBox.addItem("Freight Truck");
    VehicleListBox.addItem("Cargo Plane");
    VehicleListBox.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
        {
        }
      });
     
    VehiclesPanel.add(VehicleListBox);
    VehicleListLabel = new JLabel("Vehicle List");
    VehiclesPanel.add(VehicleListLabel);
    VehicleDefaultListModel = new DefaultListModel< String >();
    VehicleList = new  JList< String >(VehicleDefaultListModel);
    VehicleList.addMouseListener(new MouseListener(){
      public void mouseClicked(MouseEvent e)
      {
      }

      public void mousePressed(MouseEvent e){}
      public void mouseReleased(MouseEvent e){}
      public void mouseEntered(MouseEvent e){}
      public void mouseExited(MouseEvent e){}
    });
    JScrollPane vehicleScrollPane = new JScrollPane(VehicleList);
    VehiclesPanel.add(vehicleScrollPane);
    return VehiclesPanel;
  }
  
  JPanel createOutputPanel(){
    OutputPanel = new JPanel();
    OutputPanel.setLayout(new BoxLayout(OutputPanel,BoxLayout.Y_AXIS));
    Output = new JLabel("*** Output *** ");
    OutputPanel.add(Output);
    output = new JTextArea("");
    output.setSize(new Dimension(600,500));
    output.setEditable(false);
    JScrollPane realOutput = new JScrollPane(output);
    realOutput.setSize(new Dimension(600,500));
    OutputPanel.add(realOutput);
    return OutputPanel;
  }

  /**
   * Updates a parcel in the parcel DefaultListModel. If a special view is

   * selected from the ComboBox it will update the current DefaultListModel
   * either adding the parcel to it due to a change in priority or removing it.
   * This function is called from within vehicle when a parcel's priority
   * changes from High to Medium or Medium to Low. It is also called from
   * reroute parcel when rerouting a parcel changes its priority.
   *
   * @param parcel
   *     The parcel to be updated.
   * @param oldPickupPriorityLevel
   *     The parcel's previousPriority to handle updating specialized
   *     views. I.E. only showing medium priority
   *     parcels
   */

  public static JTextArea getTextArea()
  {
    // TODO Auto-generated method stub
    return output;
  }

  private static JList<String> getParcelJList()
  {
    // TODO Auto-generated method stub
    return ParcelList;
  }

  private static JComboBox getParcelTypeComboBox()
  {
    // TODO Auto-generated method stub
    return ParcelListBox;
  }





  // ---------------------------------------------------------------------------
}
