package youxi.util;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import javax.swing.*;

public class ResponsiveUtil {

    /** 计算基于父容器宽度的动态尺寸，ratio 如 0.8 表示占父容器 80%，maxPx 为上限 */
    public static Dimension dynamicWidth(Container parent, float ratio, int maxPx, int height) {
        int parentW = parent != null ? parent.getWidth() : maxPx;
        if (parentW <= 0) parentW = maxPx;
        int w = Math.min((int) (parentW * ratio), maxPx);
        if (w < 150) w = 150;
        return new Dimension(w, height);
    }

    /** 给面板添加 resize 监听，在父容器尺寸变化时重新计算子组件尺寸 */
    public static void installResizeUpdater(JComponent target, Runnable updater) {
        target.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && target.isShowing()) {
                SwingUtilities.invokeLater(updater);
            }
        });
        target.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                updater.run();
                target.revalidate();
            }
        });
    }
}
