package mysqls.ui_frame;

import mysqls.UMLEditor;
import mysqls.diagrams.ClassDiagramGraph;
import mysqls.framework.*;
import mysqls.graph.Edge;
import mysqls.graph.Graph;
import mysqls.graph.GraphElement;
import mysqls.graph.Node;
import mysqls.sql.util.MyIOutil;
import mysqls.ui_mainitem.GraphFrame;
import mysqls.ui_mainitem.GraphPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * 主框架，里面可以有很多内部框架，tab
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame {
    private static final int FRAME_GAP = 20;
    private static final int ESTIMATED_FRAMES = 5;
    private static final int MAX_RECENT_FILES = 8;
    private static final int MARGIN_SCREEN = 8; // Fraction of the screen to
    // leave around the sides
    private static final int MARGIN_IMAGE = 2; // Number of pixels to leave
    // around the graph when
    // exporting it as an image
    private static final int HELP_MENU_TEXT_WIDTH = 10; // Number of pixels to
    // give to the width of
    // the text area of the
    // Help Menu.
    private static final int HELP_MENU_TEXT_HEIGHT = 40; // Number of pixels to
    // give to the
    // height of the
    // text area of the
    // Help Menu.

    private MenuFactory aAppFactory;
    private ResourceBundle aAppResources;
    private ResourceBundle aVersionResources;
    private ResourceBundle aEditorResources;
    private JTabbedPane aTabbedPane;// 选项卡窗体。
    private ArrayList<JInternalFrame> aTabs = new ArrayList<>();
    private JMenu aNewMenu;
    private Clipboard aClipboard = new Clipboard();

    private RecentFilesQueue aRecentFiles = new RecentFilesQueue();
    private JMenu aRecentFilesMenu;

    private WelcomeTab aWelcomeTab;

    // Menus or menu items that must be disabled if there is no current diagram.
    private final List<JMenuItem> aDiagramRelevantMenus = new ArrayList<>();

    /**
     * Constructs a blank frame with a desktop pane but no graph windows.
     *
     * @param pAppClass the fully qualified app class name. It is expected that the
     *                  resources are appClassName + "Strings" and appClassName +
     *                  "Version" (the latter for version-specific resources)
     */
    public MainFrame(Class<?> pAppClass) {
        String appClassName = pAppClass.getName();
        aAppResources = ResourceBundle.getBundle(appClassName + "Strings");
        aAppFactory = new MenuFactory(aAppResources);
        aVersionResources = ResourceBundle.getBundle(appClassName + "Version");
        aEditorResources = ResourceBundle.getBundle("mysqls.framework.EditorStrings");
//		MenuFactory factory = new MenuFactory(aEditorResources);

//		aRecentFiles.deserialize(Preferences.userNodeForPackage(UMLEditor.class).get("recent", "").trim());

        setTitle(aAppResources.getString("app.name"));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();

        setBounds(screenWidth / (MainFrame.MARGIN_SCREEN * 2), screenHeight / (MainFrame.MARGIN_SCREEN * 2),
                (screenWidth * (MainFrame.MARGIN_SCREEN - 1)) / MainFrame.MARGIN_SCREEN,
                (screenHeight * (MainFrame.MARGIN_SCREEN - 1)) / MainFrame.MARGIN_SCREEN);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent pEvent) {
                exit();
            }
        });

//		aTabbedPane = new JTabbedPane();
//		aTabbedPane.addChangeListener(new ChangeListener() {
//			@Override
//			public void stateChanged(ChangeEvent pEven) {
//				boolean noGraphFrame = noCurrentGraphFrame();
//				for (JMenuItem menuItem : aDiagramRelevantMenus) {
//					menuItem.setEnabled(!noGraphFrame);
//				}
//			}
//		});
//		setContentPane(aTabbedPane);
        getmymainpanel();

//		setJMenuBar(new JMenuBar());
//
//		createFileMenu(factory);
//		createEditMenu(factory);
//		createViewMenu(factory);
//		createSQLmnu();// sqlmenu，
//		createdatabasemenu();// 数据库服务的enu
//		createHelpMenu(factory);
    }


    //	主要的内容
    private void getmymainpanel() {
        getContentPane().setLayout(new BorderLayout());
        BootPanel bootPanel = BootPanel.getInstance();
        MainleftPanel mainleftPanel = MainleftPanel.getInstance();
        MainCenterPanel centerPanel = MainCenterPanel.getInstance();
        ToolPanel toolPanel = ToolPanel.getInstance(centerPanel);
        MainPanel mainPanel = new MainPanel(mainleftPanel, centerPanel);

        getContentPane().add(bootPanel, BorderLayout.SOUTH);
        getContentPane().add(toolPanel, BorderLayout.NORTH);
        getContentPane().add(mainPanel, BorderLayout.CENTER);

    }

    /**
     * 每个内部框架都可以有不同的数据库连接，所以数据库的实现应该在内部框架里面实现，这里直接调用就行
     */
    private void createdatabasemenu() {
        // TODO Auto-generated method stub
        JMenuBar menuBar = getJMenuBar();
        JMenu dbMenu = new JMenu("数据库");
        aDiagramRelevantMenus.add(dbMenu);
        menuBar.add(dbMenu);

        JMenuItem server = new JMenuItem("连接服务器");
        server.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                GraphFrame frame = (GraphFrame) aTabbedPane.getSelectedComponent();
                frame.servermenu();
            }
        });
        dbMenu.add(server);

        JMenuItem database = new JMenuItem("数据库选择");
        database.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                GraphFrame frame = (GraphFrame) aTabbedPane.getSelectedComponent();
                frame.databasemenu();
            }
        });
        // dbMenu.add(database);

        JMenuItem db2graph = new JMenuItem("数据库操作");
        db2graph.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                GraphFrame frame = (GraphFrame) aTabbedPane.getSelectedComponent();
                frame.loaddatabasealltables();
            }
        });
        dbMenu.add(db2graph);

        JMenuItem graph2db = new JMenuItem("sql模型导入数据库");
        graph2db.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                GraphFrame frame = (GraphFrame) aTabbedPane.getSelectedComponent();
                frame.graph2dbmenu();
            }
        });
        dbMenu.add(graph2db);

        JMenuItem mysql = new JMenuItem("mysql状态变量");
        mysql.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                GraphFrame frame = (GraphFrame) aTabbedPane.getSelectedComponent();
                frame.mysqlvariablemenu();
            }
        });
        dbMenu.add(mysql);

    }

    /**
     * 增加menue，具体的功能内部框架来实现.
     */
    private void createSQLmnu() {
        // TODO Auto-generated method stub
        JMenuBar menuBar = getJMenuBar();
        JMenu sqlmenue = new JMenu("SQL建模");
        aDiagramRelevantMenus.add(sqlmenue);
        menuBar.add(sqlmenue);

        JMenuItem sql2graph = new JMenuItem("sql->图形");
        sql2graph.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                GraphFrame frame = (GraphFrame) aTabbedPane.getSelectedComponent();
                frame.sql2graph();

            }
        });
        sqlmenue.add(sql2graph);

        JMenuItem graph2sql = new JMenuItem("图形->sql");
        graph2sql.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                GraphFrame frame = (GraphFrame) aTabbedPane.getSelectedComponent();
                frame.graph2sql();
            }
        });
        sqlmenue.add(graph2sql);

    }

    private void createFileMenu(MenuFactory pFactory) {
        JMenuBar menuBar = getJMenuBar();
        JMenu fileMenu = pFactory.createMenu("file");
        menuBar.add(fileMenu);

        aNewMenu = pFactory.createMenu("file.new");
        fileMenu.add(aNewMenu);

        JMenuItem fileOpenItem = pFactory.createMenuItem("file.open", this, "openFile");
        fileMenu.add(fileOpenItem);

        aRecentFilesMenu = pFactory.createMenu("file.recent");
        buildRecentFilesMenu();
        fileMenu.add(aRecentFilesMenu);

        JMenuItem closeFileItem = pFactory.createMenuItem("file.close", this, "close");
        fileMenu.add(closeFileItem);
        aDiagramRelevantMenus.add(closeFileItem);
        closeFileItem.setEnabled(!noCurrentGraphFrame());

        JMenuItem fileSaveItem = pFactory.createMenuItem("file.save", this, "save");
        fileMenu.add(fileSaveItem);
        aDiagramRelevantMenus.add(fileSaveItem);
        fileSaveItem.setEnabled(!noCurrentGraphFrame());

        JMenuItem fileSaveAsItem = pFactory.createMenuItem("file.save_as", this, "saveAs");
        fileMenu.add(fileSaveAsItem);
        aDiagramRelevantMenus.add(fileSaveAsItem);
        fileSaveAsItem.setEnabled(!noCurrentGraphFrame());

        JMenuItem fileExportItem = pFactory.createMenuItem("file.export_image", this, "exportImage");
        fileMenu.add(fileExportItem);
        aDiagramRelevantMenus.add(fileExportItem);
        fileExportItem.setEnabled(!noCurrentGraphFrame());

        JMenuItem fileCopyToClipboard = pFactory.createMenuItem("file.copy_to_clipboard", this, "copyToClipboard");
        fileMenu.add(fileCopyToClipboard);
        aDiagramRelevantMenus.add(fileCopyToClipboard);
        fileCopyToClipboard.setEnabled(!noCurrentGraphFrame());

        fileMenu.addSeparator();

        JMenuItem fileExitItem = pFactory.createMenuItem("file.exit", this, "exit");
        fileMenu.add(fileExitItem);
    }

    private void createEditMenu(MenuFactory pFactory) {
        JMenuBar menuBar = getJMenuBar();
        JMenu editMenu = pFactory.createMenu("edit");
        menuBar.add(editMenu);
        aDiagramRelevantMenus.add(editMenu);
        editMenu.setEnabled(!noCurrentGraphFrame());

        editMenu.add(pFactory.createMenuItem("edit.undo", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent pEvent) {
                if (noCurrentGraphFrame()) {
                    return;
                }
                ((GraphFrame) aTabbedPane.getSelectedComponent()).getGraphPanel().undo();
            }
        }));

        editMenu.add(pFactory.createMenuItem("edit.redo", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent pEvent) {
                if (noCurrentGraphFrame()) {
                    return;
                }
                ((GraphFrame) aTabbedPane.getSelectedComponent()).getGraphPanel().redo();
            }
        }));

        editMenu.add(pFactory.createMenuItem("edit.selectall", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent pEvent) {
                if (noCurrentGraphFrame()) {
                    return;
                }
                ((GraphFrame) aTabbedPane.getSelectedComponent()).getGraphPanel().selectAll();
            }
        }));

        editMenu.add(pFactory.createMenuItem("edit.properties", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent pEvent) {
                if (noCurrentGraphFrame()) {
                    return;
                }
                ((GraphFrame) aTabbedPane.getSelectedComponent()).getGraphPanel().editSelected();
            }
        }));

        editMenu.add(pFactory.createMenuItem("edit.cut", this, "cut"));
        editMenu.add(pFactory.createMenuItem("edit.paste", this, "paste"));
        editMenu.add(pFactory.createMenuItem("edit.copy", this, "copy"));

        editMenu.add(pFactory.createMenuItem("edit.delete", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent pEvent) {
                if (noCurrentGraphFrame()) {
                    return;
                }
                ((GraphFrame) aTabbedPane.getSelectedComponent()).getGraphPanel().removeSelected();
            }
        }));
    }

    private void createViewMenu(MenuFactory pFactory) {
        JMenuBar menuBar = getJMenuBar();

        JMenu viewMenu = pFactory.createMenu("view");
        menuBar.add(viewMenu);
        aDiagramRelevantMenus.add(viewMenu);
        viewMenu.setEnabled(!noCurrentGraphFrame());

        viewMenu.add(pFactory.createMenuItem("view.zoom_out", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent pEvent) {
                if (noCurrentGraphFrame()) {
                    return;
                }
                ((GraphFrame) aTabbedPane.getSelectedComponent()).getGraphPanel().changeZoom(-1);
            }
        }));

        viewMenu.add(pFactory.createMenuItem("view.zoom_in", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent pEvent) {
                if (noCurrentGraphFrame()) {
                    return;
                }
                ((GraphFrame) aTabbedPane.getSelectedComponent()).getGraphPanel().changeZoom(1);
            }
        }));

        final JCheckBoxMenuItem hideGridItem = (JCheckBoxMenuItem) pFactory.createCheckBoxMenuItem("view.hide_grid",
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent pEvent) {
                        if (noCurrentGraphFrame()) {
                            return;
                        }
                        GraphFrame frame = (GraphFrame) aTabbedPane.getSelectedComponent();
                        GraphPanel panel = frame.getGraphPanel();
                        JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) pEvent.getSource();
                        panel.setHideGrid(menuItem.isSelected());
                    }
                });
        viewMenu.add(hideGridItem);

        viewMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent pEvent) {
                if (aTabbedPane.getSelectedComponent() instanceof WelcomeTab) {
                    return;
                }
                GraphFrame frame = (GraphFrame) aTabbedPane.getSelectedComponent();
                if (frame == null) {
                    return;
                }
                GraphPanel panel = frame.getGraphPanel();
                hideGridItem.setSelected(panel.getHideGrid());
            }

            @Override
            public void menuDeselected(MenuEvent pEvent) {
            }

            @Override
            public void menuCanceled(MenuEvent pEvent) {
            }
        });
    }

    private void createHelpMenu(MenuFactory pFactory) {
        JMenuBar menuBar = getJMenuBar();
        JMenu helpMenu = pFactory.createMenu("help");
        menuBar.add(helpMenu);

        helpMenu.add(pFactory.createMenuItem("help.about", this, "showAboutDialog"));
        // helpMenu.add(pFactory.createMenuItem("help.license", new
        // ActionListener() {
        // @Override
        // public void actionPerformed(ActionEvent pEvent) {
        // try {
        // BufferedReader reader = new BufferedReader(
        // new
        // InputStreamReader(getClass().getResourceAsStream("license.txt")));
        // JTextArea text = new JTextArea(MainFrame.HELP_MENU_TEXT_WIDTH,
        // MainFrame.HELP_MENU_TEXT_HEIGHT);
        // String line;
        // while ((line = reader.readLine()) != null) {
        // text.append(line);
        // text.append("\n");
        // }
        // text.setCaretPosition(0);
        // text.setEditable(false);
        // JOptionPane.showInternalMessageDialog(aTabbedPane, new
        // JScrollPane(text),
        // aEditorResources.getString("dialog.license.title"),
        // JOptionPane.PLAIN_MESSAGE);
        // } catch (IOException exception) {
        // }
        // }
        // }));
    }

    /**
     * Adds a graph type to the File->New menu.
     *
     * @param pResourceName the name of the menu item resource
     * @param pGraphClass   the class object for the graph
     */
    public void addGraphType(String pResourceName, final Class<?> pGraphClass) {
        aNewMenu.add(aAppFactory.createMenuItem(pResourceName, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent pEvent) {
                try {
                    GraphFrame frame = new GraphFrame((Graph) pGraphClass.newInstance(), aTabbedPane);
                    addTab(frame);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }));
    }

    /**
     * Sets the TaskBar icon for the application.
     */
    public void setIcon() {
        try {
            java.net.URL url = getClass().getClassLoader().getResource(aAppResources.getString("app.icon"));
            Toolkit kit = Toolkit.getDefaultToolkit();
            Image img = kit.createImage(url);
            setIconImage(img);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the command line arguments.
     *
     * @param pArgs the command line arguments
     */
    public void readArgs(String[] pArgs) {
        if (pArgs.length != 0) {
            for (String argument : pArgs) {
                open(argument);
            }
        }
        /* @JoelChev may be needed later */
        // setTitle();
    }

    /*
     * Opens a file with the given name, or switches to the frame if it is
     * already open.
     *
     * @param pName the file name
     */
    private void open(String pName) {
        for (int i = 0; i < aTabs.size(); i++) {
            if (aTabbedPane.getComponentAt(i) instanceof GraphFrame) {
                GraphFrame frame = (GraphFrame) aTabbedPane.getComponentAt(i);
                if (frame.getFileName() != null
                        && frame.getFileName().getAbsoluteFile().equals(new File(pName).getAbsoluteFile())) {
                    try {
                        frame.toFront();
                        frame.setSelected(true);
                        addRecentFile(new File(pName).getPath());
                    } catch (PropertyVetoException exception) {
                    }
                    return;
                }
            }
        }
        try {
            Graph graph = PersistenceService.read(pName);
            GraphFrame frame = new GraphFrame(graph, aTabbedPane);
            frame.setFile(new File(pName).getAbsoluteFile());
            addRecentFile(new File(pName).getPath());
            addTab(frame);
        } catch (Exception exception) {
            System.out.println(exception.getStackTrace());
            JOptionPane.showMessageDialog(aTabbedPane, exception.getMessage(),
                    aEditorResources.getString("file.open.text"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /*
     * Adds an InternalFrame to the list of Tabs.
     *
     * @param c the component to display in the internal frame
     *
     * @param t the title of the internal frame.
     */
    private void addTab(final JInternalFrame pInternalFrame) {
        int frameCount = aTabbedPane.getComponentCount();
        BasicInternalFrameUI ui = (BasicInternalFrameUI) pInternalFrame.getUI();
        Container north = ui.getNorthPane();
        north.remove(0);
        north.validate();
        north.repaint();
        aTabbedPane.add(setTitle(pInternalFrame), pInternalFrame);
        int i = aTabs.size();
        aTabbedPane.setTabComponentAt(i, new ButtonTabComponent(this, pInternalFrame, aTabbedPane));
        aTabs.add(pInternalFrame);
        // position frame
        int emptySpace = MainFrame.FRAME_GAP * Math.max(MainFrame.ESTIMATED_FRAMES, frameCount);
        int width = Math.max(aTabbedPane.getWidth() / 2, aTabbedPane.getWidth() - emptySpace);
        int height = Math.max(aTabbedPane.getHeight() / 2, aTabbedPane.getHeight() - emptySpace);

        pInternalFrame.reshape(frameCount * MainFrame.FRAME_GAP, frameCount * MainFrame.FRAME_GAP, width, height);
        pInternalFrame.show();
        int last = aTabs.size();
        aTabbedPane.setSelectedIndex(last - 1);
        if (aTabbedPane.getComponentAt(0) instanceof WelcomeTab) {
            removeWelcomeTab();
        }

    }

    /**
     * @param pInternalFrame The current frame to give a Title in its tab.
     * @return The title of a given tab.
     */
    private String setTitle(JInternalFrame pInternalFrame) {
        String appName = aAppResources.getString("app.name");
        String diagramName = "";

        if (pInternalFrame == null || !(pInternalFrame instanceof GraphFrame)) {
            return appName;
        } else {
            GraphFrame frame = (GraphFrame) pInternalFrame;
            File file = frame.getFileName();
            if (file == null) {
                Graph graphType = frame.getGraph();
                if (graphType instanceof ClassDiagramGraph) {
                    diagramName = "SQL数据建模图";

                }
                // else if(graphType instanceof ObjectDiagramGraph)
                // {
                // diagramName = "Object Diagram";
                // }
                // else if(graphType instanceof UseCaseDiagramGraph)
                // {
                // diagramName = "Use Case Diagram";
                // }
                // else if(graphType instanceof StateDiagramGraph)
                // {
                // diagramName = "State Diagram";
                // }
                // else
                // {
                // diagramName = "Sequence Diagram";
                // }

                return diagramName;
            } else {
                return file.getName();
            }
        }
    }

    /**
     * This adds a WelcomeTab to the tabs. This is only done if all other tabs
     * have been previously closed.
     */
    public void addWelcomeTab() {
        aWelcomeTab = new WelcomeTab(aNewMenu, aRecentFilesMenu);
        aTabbedPane.add("Welcome", aWelcomeTab);
        aTabs.add(aWelcomeTab);
    }

    /**
     * This method removes the WelcomeTab after a file has been opened or a
     * diagram starts being created.
     */
    public void removeWelcomeTab() {
        if (aWelcomeTab != null) {
            aTabbedPane.remove(0);
            aTabs.remove(0);
        }
    }

    /**
     * @param pInternalFrame The JInternalFrame to remove. Calling this metod will remove a
     *                       given JInternalFrame.
     */
    public void removeTab(final JInternalFrame pInternalFrame) {
        if (!aTabs.contains(pInternalFrame)) {
            return;
        }
        JTabbedPane tp = aTabbedPane;
        int pos = aTabs.indexOf(pInternalFrame);
        tp.remove(pos);
        aTabs.remove(pInternalFrame);
        if (aTabs.size() == 0) {
            aWelcomeTab = new WelcomeTab(aNewMenu, aRecentFilesMenu);
            aTabbedPane.add("Welcome", aWelcomeTab);
            aTabs.add(aWelcomeTab);
        }
    }

    /*
     * Adds a file name to the "recent files" list and rebuilds the
     * "recent files" menu.
     *
     * @param pNewFile the file name to add
     */
    private void addRecentFile(String pNewFile) {
        aRecentFiles.add(pNewFile);
        buildRecentFilesMenu();
    }

    /*
     * Rebuilds the "recent files" menu. Only works if the number of recent
     * files is less than 8. Otherwise, additional logic will need to be added
     * to 0-index the mnemonics for files 1-9
     */
    private void buildRecentFilesMenu() {
        assert aRecentFiles.size() <= MainFrame.MAX_RECENT_FILES;
        aRecentFilesMenu.removeAll();
        aRecentFilesMenu.setEnabled(aRecentFiles.size() > 0);
        int i = 1;
        for (File file : aRecentFiles) {
            String name = i + " " + file.getName();
            final String fileName = file.getAbsolutePath();
            JMenuItem item = new JMenuItem(name);
            item.setMnemonic('0' + i);
            aRecentFilesMenu.add(item);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent pEvent) {
                    open(fileName);
                }
            });
            i++;
        }
    }

    /**
     * Asks the user to open a graph file.
     */
    public void openFile() {
        JFileChooser fileChooser = new JFileChooser(aRecentFiles.getMostRecentDirectory());
        fileChooser.setFileFilter(
                new ExtensionFilter(aAppResources.getString("files.name"), aAppResources.getString("files.extension")));
        // TODO This Editor frame should keep a list of graph types to make this
        // operation not hard-code them
        ExtensionFilter[] filters = new ExtensionFilter[]{
                new ExtensionFilter(aAppResources.getString("state.name"),
                        aAppResources.getString("state.extension") + aAppResources.getString("files.extension")),
                new ExtensionFilter(aAppResources.getString("object.name"),
                        aAppResources.getString("object.extension") + aAppResources.getString("files.extension")),
                new ExtensionFilter(aAppResources.getString("class.name"),
                        aAppResources.getString("class.extension") + aAppResources.getString("files.extension")),
                new ExtensionFilter(aAppResources.getString("usecase.name"),
                        aAppResources.getString("usecase.extension") + aAppResources.getString("files.extension")),
                new ExtensionFilter(aAppResources.getString("sequence.name"),
                        aAppResources.getString("sequence.extension") + aAppResources.getString("files.extension"))};
        for (ExtensionFilter filter : filters) {
            fileChooser.addChoosableFileFilter(filter);
        }
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            open(file.getAbsolutePath());
        }
    }

    /**
     * Cuts the current selection of the current panel and puts the content into
     * the application-specific clipboard.
     */
    public void cut() {
        if (noCurrentGraphFrame()) {
            return;
        }
        GraphFrame frame = (GraphFrame) aTabbedPane.getSelectedComponent();
        GraphPanel panel = frame.getGraphPanel();
        Graph curGraph = frame.getGraph();
        if (panel.getSelectionList().size() > 0) {
            SelectionList currentSelection = panel.getSelectionList();
            aClipboard.copy(currentSelection);
            Iterator<GraphElement> iter = currentSelection.iterator();
            panel.startCompoundListening();
            while (iter.hasNext()) {
                GraphElement element = iter.next();
                if (element instanceof Edge) {
                    curGraph.removeEdge((Edge) element);
                } else {
                    curGraph.removeNode((Node) element);
                }
                iter.remove();
            }
            panel.endCompoundListening();
        }
        panel.repaint();
    }

    /**
     * Copies the current selection of the current panel and puts the content
     * into the application-specific clipboard.
     */
    public void copy() {
        if (noCurrentGraphFrame()) {
            return;
        }
        GraphFrame frame = (GraphFrame) aTabbedPane.getSelectedComponent();
        GraphPanel panel = frame.getGraphPanel();
        if (panel.getSelectionList().size() > 0) {
            SelectionList currentSelection = panel.getSelectionList();
            aClipboard.copy(currentSelection);
        }
    }

    /**
     * Pastes a past selection from the application-specific Clipboard into
     * current panel. All the logic is done in the application-specific
     * CutPasteBehavior.
     */
    public void paste() {
        if (noCurrentGraphFrame()) {
            return;
        }
        GraphFrame frame = (GraphFrame) aTabbedPane.getSelectedComponent();

        GraphPanel panel = frame.getGraphPanel();

        SelectionList updatedSelectionList = aClipboard.paste(panel);
        panel.setSelectionList(updatedSelectionList);
        panel.repaint();

    }

    /**
     * Copies the current image to the clipboard.
     */
    public void copyToClipboard() {
        if (noCurrentGraphFrame()) {
            return;
        }
        GraphFrame frame = (GraphFrame) aTabbedPane.getSelectedComponent();
        final BufferedImage image = MainFrame.getImage(frame.getGraph());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new Transferable() {
            @Override
            public boolean isDataFlavorSupported(DataFlavor pFlavor) {
                return DataFlavor.imageFlavor.equals(pFlavor);
            }

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{DataFlavor.imageFlavor};
            }

            @Override
            public Object getTransferData(DataFlavor pFlavor) throws UnsupportedFlavorException, IOException {
                if (DataFlavor.imageFlavor.equals(pFlavor)) {
                    return image;
                } else {
                    throw new UnsupportedFlavorException(pFlavor);
                }
            }
        }, null);
        JOptionPane.showInternalMessageDialog(aTabbedPane, aEditorResources.getString("dialog.to_clipboard.message"),
                aEditorResources.getString("dialog.to_clipboard.title"), JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean noCurrentGraphFrame() {
        return aTabbedPane.getSelectedComponent() == null
                || !(aTabbedPane.getSelectedComponent() instanceof GraphFrame);
    }

    /**
     * If a user confirms that they want to close their modified graph, this
     * method will remove it from the current list of tabs.
     */
    public void close() {
        if (noCurrentGraphFrame()) {
            return;
        }
        JInternalFrame curFrame = (JInternalFrame) aTabbedPane.getSelectedComponent();
        if (curFrame != null) {
            GraphFrame openFrame = (GraphFrame) curFrame;
            // we only want to check attempts to close a frame
            if (openFrame.getGraphPanel().isModified()) {
                // ask user if it is ok to close
                if (JOptionPane.showConfirmDialog(openFrame, aEditorResources.getString("dialog.close.ok"), null,
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    removeTab(curFrame);
                }
                return;
            } else {
                removeTab(curFrame);
            }
        }
    }

    /**
     * If a user confirms that they want to close their modified graph, this
     * method will remove it from the current list of tabs.
     *
     * @param pJInternalFrame The current JInternalFrame that one wishes to close.
     */
    public void close(JInternalFrame pJInternalFrame) {
        JInternalFrame curFrame = pJInternalFrame;
        if (curFrame != null) {
            GraphFrame openFrame = (GraphFrame) curFrame;
            // we only want to check attempts to close a frame
            if (openFrame.getGraphPanel().isModified()) {
                if (JOptionPane.showConfirmDialog(openFrame, aEditorResources.getString("dialog.close.ok"), null,
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    removeTab(curFrame);
                }
                return;
            }
            removeTab(curFrame);
        }
    }

    /**
     * Save a file. Called by reflection.
     */
    public void save() {
        if (noCurrentGraphFrame()) {
            return;
        }
        GraphFrame frame = (GraphFrame) aTabbedPane.getSelectedComponent();
        File file = frame.getFileName();
        if (file == null) {
            saveAs();
            return;
        }
        try {
            MyIOutil.savefile(frame.getGraph(), file);
            // PersistenceService.saveFile(frame.getGraph(), new
            // FileOutputStream(file));
            frame.getGraphPanel().setModified(false);
        } catch (Exception exception) {
            JOptionPane.showInternalMessageDialog(aTabbedPane, exception);
        }
    }

    /**
     * Saves the current graph as a new file.
     */
    public void saveAs() {
        if (noCurrentGraphFrame()) {
            return;
        }
        GraphFrame frame = (GraphFrame) aTabbedPane.getSelectedComponent();
        Graph graph = frame.getGraph();
        try {
            File result = null;

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new ExtensionFilter(graph.getDescription(), graph.getFileExtension()));
            fileChooser.setCurrentDirectory(new File("."));

            if (frame.getFileName() != null) {
                fileChooser.setSelectedFile(frame.getFileName());
            } else {
                fileChooser.setSelectedFile(new File(""));
            }
            int response = fileChooser.showSaveDialog(this);
            if (response == JFileChooser.APPROVE_OPTION) {
                File f = fileChooser.getSelectedFile();
                if (!fileChooser.getFileFilter().accept(f)) {
                    f = new File(f.getPath() + graph.getFileExtension());
                }

                if (!f.exists()) {
                    result = f;
                } else {
                    ResourceBundle editorResources = ResourceBundle.getBundle("uestc.uml.sql.framework.EditorStrings");
                    int theresult = JOptionPane.showConfirmDialog(this, editorResources.getString("dialog.overwrite"),
                            null, JOptionPane.YES_NO_OPTION);
                    if (theresult == JOptionPane.YES_OPTION) {
                        result = f;
                    }
                }
            }

            if (result != null) {
                OutputStream out = new FileOutputStream(result);
                try {
                    // PersistenceService.saveFile(graph, out);
                    MyIOutil.savefile(frame.getGraph(), result);
                } finally {
                    out.close();
                }
                addRecentFile(result.getAbsolutePath());
                frame.setFile(result);
                aTabbedPane.setTitleAt(aTabbedPane.getSelectedIndex(), frame.getFileName().getName());
                frame.getGraphPanel().setModified(false);
            }
        } catch (IOException exception) {
            JOptionPane.showInternalMessageDialog(aTabbedPane, exception);
        }
    }

    /**
     * Edits the file path so that the pToBeRemoved extension, if found, is
     * replaced with pDesired.
     *
     * @param pOriginal    the file to use as a starting point
     * @param pToBeRemoved the extension that is to be removed before adding the desired
     *                     extension.
     * @param pDesired     the desired extension (e.g. ".png")
     * @return original if it already has the desired extension, or a new file
     * with the edited file path
     */
    static String replaceExtension(String pOriginal, String pToBeRemoved, String pDesired) {
        assert pOriginal != null && pToBeRemoved != null && pDesired != null;

        if (pOriginal.endsWith(pToBeRemoved)) {
            return pOriginal.substring(0, pOriginal.length() - pToBeRemoved.length()) + pDesired;
        } else {
            return pOriginal;
        }
    }

    /**
     * Exports the current graph to an image file.
     */
    public void exportImage() {
        if (noCurrentGraphFrame()) {
            return;
        }
        GraphFrame frame = (GraphFrame) aTabbedPane.getSelectedComponent();

        File file = chooseFileToExportTo();
        if (file == null) {
            return;
        }

        // Validate the file format
        String fileName = file.getPath();
        String format = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (!ImageIO.getImageWritersByFormatName(format).hasNext()) {
            JOptionPane.showInternalMessageDialog(aTabbedPane, aEditorResources.getString("error.unsupported_image"),
                    aEditorResources.getString("error.unsupported_image.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        confirmFileOverwrite(file);

        try (OutputStream out = new FileOutputStream(file)) {
            ImageIO.write(MainFrame.getImage(frame.getGraph()), format, out);
        } catch (IOException exception) {
            JOptionPane.showInternalMessageDialog(aTabbedPane, exception);
        }
    }

    private static String[] getAllSupportedImageWriterFormats() {
        String[] names = ImageIO.getWriterFormatNames();
        HashSet<String> formats = new HashSet<String>();
        for (String name : names) {
            formats.add(name.toLowerCase());
        }
        String[] lReturn = formats.toArray(new String[formats.size()]);
        Arrays.sort(lReturn);
        return lReturn;
    }

    /* Creates a file filter for pFomat, where pFormat is in all-lowercases */
    private FileFilter createFileFilter(final String pFormat) {
        return new FileFilter() {
            @Override
            public String getDescription() {
                return pFormat.toUpperCase() + " " + aEditorResources.getString("files.image.name");
            }

            @Override
            public boolean accept(File pFile) {
                return !pFile.isDirectory() && (pFile.getName().endsWith("." + pFormat.toLowerCase())
                        || pFile.getName().endsWith("." + pFormat.toUpperCase()));
            }

            /*
             * It is important that toString returns exactly the format string
             * because the chooseFileToExportTo method relies on this
             * convention.
             */
            @Override
            public String toString() {
                return pFormat;
            }
        };
    }

    /*
     * Can return null if no file is selected.
     */
    private File chooseFileToExportTo() {
        GraphFrame frame = (GraphFrame) aTabbedPane.getSelectedComponent();
        assert frame != null;

        // Initialize the file chooser widget
        JFileChooser fileChooser = new JFileChooser();
        for (String format : MainFrame.getAllSupportedImageWriterFormats()) {
            fileChooser.addChoosableFileFilter(createFileFilter(format));
        }
        fileChooser.setCurrentDirectory(new File("."));

        // If the file was previously saved, use that to suggest a file name
        // root.
        if (frame.getFileName() != null) {
            File f = new File(MainFrame.replaceExtension(frame.getFileName().getAbsolutePath(),
                    aAppResources.getString("files.extension"), ""));
            fileChooser.setSelectedFile(f);
        }

        File file = null;
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            FileFilter selectedFilter = fileChooser.getFileFilter();

            if (!selectedFilter.accept(file) && selectedFilter != fileChooser.getAcceptAllFileFilter()) {
                file = new File(file.getPath() + "." + selectedFilter.getDescription()
                        .substring(0, selectedFilter.toString().length()).toLowerCase());
            }
        }
        return file;
    }

    /*
     * Checks if pFile would be overwritten and, if yes, asks for a
     * confirmation. If the confirmation is denied, returns null.
     */
    private File confirmFileOverwrite(File pFile) {
        if (!pFile.exists()) {
            return pFile;
        }

        ResourceBundle editorResources = ResourceBundle.getBundle("uestc.uml.sql.framework.EditorStrings");
        int result = JOptionPane.showConfirmDialog(this, editorResources.getString("dialog.overwrite"), null,
                JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            return pFile;
        } else {
            return null;
        }
    }

    /*
     * Return the image corresponding to the graph.
     *
     * @param pGraph The graph to convert to an image.
     *
     * @return bufferedImage. To convert it into an image, use the syntax :
     * Toolkit.getDefaultToolkit().createImage(bufferedImage.getSource());
     */
    private static BufferedImage getImage(Graph pGraph) {
        Rectangle2D bounds = pGraph.getBounds();
        BufferedImage image = new BufferedImage((int) (bounds.getWidth() + MainFrame.MARGIN_IMAGE * 2),
                (int) (bounds.getHeight() + MainFrame.MARGIN_IMAGE * 2), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.translate(-bounds.getX(), -bounds.getY());
        g2.setColor(Color.WHITE);
        g2.fill(new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth() + MainFrame.MARGIN_IMAGE * 2,
                bounds.getHeight() + MainFrame.MARGIN_IMAGE * 2));
        g2.translate(MainFrame.MARGIN_IMAGE, MainFrame.MARGIN_IMAGE);
        g2.setColor(Color.BLACK);
        g2.setBackground(Color.WHITE);
        pGraph.draw(g2, null);
        return image;
    }

    /**
     * Displays the About dialog box.
     */
    public void showAboutDialog() {
        MessageFormat formatter = new MessageFormat(aEditorResources.getString("dialog.about.version"));
        JOptionPane.showInternalMessageDialog(aTabbedPane,
                formatter.format(new Object[]{aAppResources.getString("app.name"),
                        aVersionResources.getString("version.number"), aVersionResources.getString("version.date"),
                        aAppResources.getString("app.copyright"), ""}),
                new MessageFormat(aEditorResources.getString("dialog.about.title"))
                        .format(new Object[]{aAppResources.getString("app.name")}),
                JOptionPane.INFORMATION_MESSAGE,
                new ImageIcon(getClass().getClassLoader().getResource(aAppResources.getString("app.icon"))));

    }

    /**
     * Exits the program if no graphs have been modified or if the user agrees
     * to abandon modified graphs.
     */
    public void exit() {
        int modcount = 0;
        for (int i = 0; i < aTabs.size(); i++) {
            if (aTabs.get(i) instanceof GraphFrame) {
                GraphFrame frame = (GraphFrame) aTabs.get(i);
                if (frame.getGraphPanel().isModified()) {
                    modcount++;
                }
            }
        }
        if (modcount > 0) {
            // ask user if it is ok to close
            int result = JOptionPane.showInternalConfirmDialog(aTabbedPane, MessageFormat
                            .format(aEditorResources.getString("dialog.exit.ok"), new Integer(modcount)), null,
                    JOptionPane.YES_NO_OPTION);

            // if the user doesn't agree, veto the close
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }
        Preferences.userNodeForPackage(UMLEditor.class).put("recent", aRecentFiles.serialize());
        System.exit(0);
    }
}
