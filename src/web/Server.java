package web;

import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URI;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

public class Server {
    public static void main(String[] args) throws Exception {
        String docRoot = ".";
        String port = scanPort();
        // String port = "8081";
        boolean browse = true;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--docRoot") || args[i].equals("-d")) {
                docRoot = args[i + 1];
            }
            if (args[i].equals("--port") || args[i].equals("-p")) {
                port = args[i + 1];
            }
            if (args[i].equals("--open_brower") || args[i].equals("-o")) {
                browse = true;
            }
            if (args[i].equals("--help") || args[i].equals("-?")
                    || args[i].equals("-h")) {

                System.out
                        .println("java -jar simple-web-server.jar [-d <docRoot>] [-p <port>] [-o] [-?]");

                return;
            }
        }

        new Server().start(docRoot, Integer.parseInt(port), browse);
    }

    private static String scanPort() throws Exception {
        System.out.println("scan port...");

        int p = 8080;
        for (; p < 65536; p++) {
            System.out.print("\t" + p + "\t...");

            Socket socket = null;
            try {
                socket = new Socket("localhost", p);
                System.out.println("used.");
            } catch (ConnectException e) {
                System.out.println("OK.");
                break;
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (Exception e) {}
                }
            }
        }
        if (p == 65536) {
            throw new Exception("port not found");
        }
        return "" + p;
    }

    public void start(String docRoot, int port, boolean browse)
            throws Exception {
        final ResourceBundle bundle = ResourceBundle.getBundle("strings");

        final org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(
                port);
        server.setHandler(resourceHandler(docRoot));
        server.start();

        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }

        String labelText = new File(".").getCanonicalPath() + " : " + port;
        if (labelText.length() > 38) {
            labelText = "..." + labelText.substring(labelText.length() - 38);
        }
        // final JFrame frame = new JFrame(bundle.getString("app.name"));
        final JFrame frame = new JFrame(labelText);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 80);
        // frame.setAlwaysOnTop(true);
        frame.setLayout(new FlowLayout());
        frame.setResizable(false);
        frame.setVisible(true);

        final JButton stopButton = new JButton(
                bundle.getString("stop.button.label"));
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        frame.add(stopButton);

        frame.setIconImage(ImageIO.read(getClass().getResource("/icon.png")));

        // move frame to left-top
        final GraphicsEnvironment env = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        final GraphicsDevice device = env.getDefaultScreenDevice();
        final Rectangle screenRect = device.getDefaultConfiguration()
                .getBounds();
        frame.setLocation(screenRect.width - frame.getWidth(), 0);

        // start browser
        if (browse) {
            Desktop.getDesktop().browse(new URI("http://localhost:" + port));
        }
    }

    private HandlerList resourceHandler(String docRoot) {
        final HandlerList list = new HandlerList();
        final ResourceHandler rh = new ResourceHandler() {
            @Override
            public void handle(String target, Request baseRequest,
                    HttpServletRequest request, HttpServletResponse response)
                    throws IOException, ServletException {
                super.handle(target, baseRequest, request, response);

                response.addHeader("Cache-Control", "no-cache");
                response.addHeader("Pragma", "no-cache");
                response.addHeader("Expires", "Thu, 01 Dec 1994 16:00:00 GMT");
            }
        };
        rh.setResourceBase(docRoot);
        list.addHandler(rh);
        return list;
    }
}
