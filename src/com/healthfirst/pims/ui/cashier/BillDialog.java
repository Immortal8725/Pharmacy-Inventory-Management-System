package com.healthfirst.pims.ui.cashier;

import com.healthfirst.pims.model.CartItem;
import com.healthfirst.pims.model.Sale;
import com.healthfirst.pims.model.SaleItem;
import com.healthfirst.pims.ui.components.FormSupport;
import com.healthfirst.pims.ui.theme.UITheme;
import com.healthfirst.pims.util.CurrencyUtil;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BillDialog extends JDialog implements Printable {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final JTextArea billArea = new JTextArea();
    private final String billText;

    public BillDialog(Window owner, Sale sale, List<CartItem> cartSnapshot) {
        super(owner, "Customer bill — HealthFirst Pharmacy", ModalityType.APPLICATION_MODAL);
        this.billText = buildBill(sale, cartSnapshot);
        setSize(520, 640);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        billArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        billArea.setEditable(false);
        billArea.setText(billText);
        billArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 18, 16, 18));
        add(new JScrollPane(billArea), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        buttons.setBackground(Color.WHITE);
        JButton save = UITheme.secondaryButton("Save bill");
        save.addActionListener(e -> saveBill());
        JButton print = UITheme.secondaryButton("Print");
        print.addActionListener(e -> printBill());
        JButton close = UITheme.primaryButton("Close");
        close.addActionListener(e -> dispose());
        buttons.add(save);
        buttons.add(print);
        buttons.add(close);
        add(buttons, BorderLayout.SOUTH);
    }

    public static void showBill(java.awt.Component parent, Sale sale, List<CartItem> cartSnapshot) {
        Window owner = parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
        new BillDialog(owner, sale, cartSnapshot).setVisible(true);
    }

    public String getBillText() {
        return billText;
    }

    private String buildBill(Sale sale, List<CartItem> cartSnapshot) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("        HEALTHFIRST PHARMACY\n");
        sb.append("     Care you can count on\n");
        sb.append("========================================\n");
        sb.append(String.format("Receipt #: %05d%n", sale.getSaleId()));
        sb.append("Date     : ").append(sale.getSaleDate() == null ? "" : DATE.format(sale.getSaleDate())).append('\n');
        sb.append("Cashier  : ").append(sale.getCashierName()).append('\n');
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-22s %3s %10s%n", "Item", "Qty", "Amount"));
        sb.append("----------------------------------------\n");

        if (cartSnapshot != null && !cartSnapshot.isEmpty()) {
            for (CartItem item : cartSnapshot) {
                String name = truncate(item.getMedicine().getName(), 22);
                sb.append(String.format("%-22s %3d %10s%n",
                        name, item.getQuantity(), CurrencyUtil.format(item.getLineTotal())));
            }
        } else {
            for (SaleItem item : sale.getItems()) {
                String name = truncate(item.getMedicineName(), 22);
                sb.append(String.format("%-22s %3d %10s%n",
                        name, item.getQuantitySold(), CurrencyUtil.format(item.getLineTotal())));
            }
        }

        sb.append("----------------------------------------\n");
        sb.append(String.format("%-26s %10s%n", "TOTAL", CurrencyUtil.format(sale.getTotalAmount())));
        sb.append("========================================\n");
        sb.append("Items sold are not returnable once the\n");
        sb.append("seal is broken. Keep this receipt.\n\n");
        sb.append("Thank you. Get well soon.\n");
        sb.append("HealthFirst Pharmacy  ·  South Africa\n");
        return sb.toString();
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }

    private void saveBill() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save customer bill");
        chooser.setSelectedFile(new java.io.File("HealthFirst_Receipt.txt"));
        chooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.writeString(chooser.getSelectedFile().toPath(), billText);
                FormSupport.info(this, "Bill saved to " + chooser.getSelectedFile().getAbsolutePath());
            } catch (IOException ex) {
                FormSupport.error(this, "Could not save the bill: " + ex.getMessage());
            }
        }
    }

    private void printBill() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("HealthFirst Receipt");
        job.setPrintable(this);
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                FormSupport.error(this, "Printing failed: " + ex.getMessage());
            }
        }
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2 = (Graphics2D) graphics;
        g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        billArea.printAll(g2);
        return PAGE_EXISTS;
    }
}
