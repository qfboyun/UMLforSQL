package mysqls.ui_mainitem;

import mysqls.contanst.ConnectINFO;
import mysqls.ui_frame.OP_Panel;
import mysqls.ui_util.sql_complementProvider;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiang on 2016/10/1 0001.
 * 下面的面板
 */
public class SQLeditPanel extends JPanel implements ActionListener {
    private static SQLeditPanel me = null;
    private OP_Panel oppanel;

    public static SQLeditPanel getInstance() {
        if (me == null) {
            me = new SQLeditPanel();
        }
        return me;
    }

    private JSplitPane jSplitPane;
    private RSyntaxTextArea textArea;
    private SQL_resultPanel sql_resultPanel;

    private SQLeditPanel() {
        setLayout(new BorderLayout());
        textArea = new RSyntaxTextArea(100, 60);
        Font font = new Font(null, 0, 20);
        textArea.setFont(font);
        textArea.setLineWrap(true);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        textArea.setCodeFoldingEnabled(true);

        // A CompletionProvider is what knows of all possible completions, and
        // analyzes the contents of the text area at the caret position to
        // determine what completion choices should be presented. Most instances
        // of CompletionProvider (such as DefaultCompletionProvider) are designed
        // so that they can be shared among multiple text components.
        CompletionProvider provider = new sql_complementProvider();
        // An AutoCompletion acts as a "middle-man" between a text component
        // and a CompletionProvider. It manages any options associated with
        // the auto-completion (the popup trigger key, whether to display a
        // documentation window along with completion choices, etc.). Unlike
        // CompletionProviders, instances of AutoCompletion cannot be shared
        // among multiple text components.
        AutoCompletion ac = new AutoCompletion(provider);
        ac.setAutoCompleteEnabled(true);
        ac.setAutoActivationEnabled(true);
        ac.setAutoActivationDelay(10);
        ac.install(textArea);
        setOpaque(false);
        OP_Panel op_panel = new OP_Panel();
        setoppanel(op_panel);
        sql_resultPanel = new SQL_resultPanel();
        add(op_panel, BorderLayout.NORTH);

        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
//        textArea.setPreferredSize(new Dimension(800,300));
//        scrollPane.setPreferredSize(new Dimension(800,600));
        jSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        jSplitPane.setDividerLocation(0.9);
        jSplitPane.setDividerSize(5);
        jSplitPane.setTopComponent(scrollPane);
        jSplitPane.setBottomComponent(sql_resultPanel);
        jSplitPane.setResizeWeight(1.0);
        jSplitPane.setDividerLocation(1.0);

        add(jSplitPane, BorderLayout.CENTER);
        add(new SQL_edit_right_tishiPanel(), BorderLayout.EAST);

    }


    private void setoppanel(OP_Panel oppanel) {
        oppanel.additem("清空编辑器", "16x16/delete.png", this);
        oppanel.additem("执行语句", "16x16/redo.png", this);
        this.oppanel = oppanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase("清空编辑器")) {
            textArea.setText("");
            return;
        }
        if (e.getActionCommand().equalsIgnoreCase("执行语句")) {
            ResultSet resultSet = exeSQL(textArea.getText().trim());
            if (resultSet != null) {
                showresult(resultSet);
            }
            return;
        }
//        JOptionPane.showMessageDialog(null,e.getActionCommand());

    }

    private void showresult(ResultSet resultSet) {
        sql_resultPanel.setResultSet(resultSet);
        jSplitPane.resetToPreferredSizes();
        updateUI();

    }

    /**
     * 事务里面执行
     */
    private ResultSet exeSQL(String trim) {
        if (ConnectINFO.getInstance().getConnection() == null) {
            JOptionPane.showMessageDialog(null, "请先链接数据库！！！");
            return null;
        }
        if (ConnectINFO.getInstance().getDatabase() == null) {
            JOptionPane.showMessageDialog(null, "请先选择数据库！！！");
            return null;

        }
        java.util.List<String> sqList = new ArrayList<>();
        for (String string : trim.split(";")) {

            String temp = string.trim();
            if (temp != null && temp.length() > 2) {
                sqList.add(temp);
            }
        }
        if (sqList.size() < 1) {
            JOptionPane.showMessageDialog(null, "没有sql语句！！！");
            return null;

        }
        Statement statement = null;
        try {
            /*有select语句*/
            String select = getselectstatement(sqList);
            if (select != null) {
                statement = ConnectINFO.getInstance().getConnection().createStatement();
                statement.execute("use " + ConnectINFO.getInstance().getDatabase().getName());
                return statement.executeQuery(select);
            }

        } catch (SQLException e) {
            try {
                ConnectINFO.getInstance().getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null, e1.getMessage());
            }
            JOptionPane.showMessageDialog(null, e.getMessage());
            e.printStackTrace();
        }
        exeupdate(sqList);
        return null;


    }

    /**
     * 事务里面执行update
     */
    private void exeupdate(List<String> sqList) {
        Statement statement = null;
        boolean ok = true;
        StringBuilder builder = new StringBuilder();
        try {
            statement = ConnectINFO.getInstance().getConnection().createStatement();
            statement.execute("use " + ConnectINFO.getInstance().getDatabase().getName());
            for (String sql : sqList) {
                statement.execute(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            builder.append(e.getMessage() + "\n");
            ok = false;
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    ok = false;
                    e.printStackTrace();
                    builder.append(e.getMessage() + "\n");
                }
            }
        }
        if (ok) {
            JOptionPane.showMessageDialog(null, "执行成功！！！");
        } else {
            JOptionPane.showMessageDialog(null, "有些语句有错误！！！" + "\n" + builder.toString());

        }

    }

    private String getselectstatement(List<String> sqList) {
        for (String s : sqList) {
            if (s.startsWith("select")) {
                return s;
            }
        }
        return null;
    }

    public void setsql(String string) {
        if (textArea != null) {
            textArea.setText(string);
        }

    }
}
