package jd.core.test;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

import javax.swing.tree.DefaultMutableTreeNode;

public class LocalVariables extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 1L;

    static DataFlavor[] flavors = { DataFlavor.stringFlavor };

    Object getTransferData(DataFlavor flavor) throws Exception {
        Object returnObject;
        if (!flavor.equals(flavors[0])) {
            throw new UnsupportedFlavorException(flavor);
        }
        Object userObject = getUserObject();
        returnObject = userObject;
        return returnObject;
    }

    double compute(double value, double length, double a, double b) {
        double result;
        if (a != b) {
            if (a < b) {
                result = value / -length * (a - b);
            } else {
                result = value / -length * (b - a);
                result -= 255.0D;
            }
            result = Math.abs(result);
        } else {
            result = a;
        }
        return result;
    }
}
