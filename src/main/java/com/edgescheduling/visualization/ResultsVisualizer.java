package com.edgescheduling.visualization;

import com.edgescheduling.EdgeSchedulingSimulation.ComparisonResult;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.*;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.block.BlockBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.GeneralPath;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ResultsVisualizer {

    private static final Font TITLE_FONT      = new Font("Times New Roman", Font.BOLD, 16);
    private static final Font AXIS_LABEL_FONT = new Font("Times New Roman", Font.BOLD, 14);
    private static final Font TICK_FONT       = new Font("Times New Roman", Font.PLAIN, 12);
    private static final Font LEGEND_FONT     = new Font("Times New Roman", Font.PLAIN, 12);

    private static final Color CPOP_COLOR = new Color(31, 119, 180);   // Blue
    private static final Color PSO_COLOR = new Color(255, 127, 14);    // Orange
    private static final Color GRID_COLOR = new Color(200, 200, 200);  // Light gray
    private static final Color BACKGROUND_COLOR = new Color(250, 250, 250); // Off-white

    private static final Color[] CHART_COLORS = {
            new Color(44, 160, 44),    // Green
            new Color(214, 39, 40),    // Red
            new Color(148, 103, 189),  // Purple
            new Color(140, 86, 75),    // Brown
            new Color(227, 119, 194),  // Pink
            new Color(127, 127, 127)   // Gray
    };

    public void generateComparisonCharts(List<ComparisonResult> results) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Edge Scheduling Algorithm Comparison");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new GridLayout(2, 2, 15, 15));
            frame.getContentPane().setBackground(Color.WHITE);

            JPanel contentPanel = new JPanel(new GridLayout(2, 2, 15, 15));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            contentPanel.setBackground(Color.WHITE);

            JFreeChart makespanChart = createMakespanComparisonChart(results);
            JFreeChart energyChart = createEnergyComparisonChart(results);
            JFreeChart qosChart = createQoSComparisonChart(results);
            JFreeChart scalabilityChart = createScalabilityChart(results);

            contentPanel.add(createEnhancedChartPanel(makespanChart));
            contentPanel.add(createEnhancedChartPanel(energyChart));
            contentPanel.add(createEnhancedChartPanel(qosChart));
            contentPanel.add(createEnhancedChartPanel(scalabilityChart));

            frame.add(contentPanel);
            frame.pack();
            frame.setSize(1400, 1000);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            saveCharts(makespanChart, energyChart, qosChart, scalabilityChart);
        });
    }

    private ChartPanel createEnhancedChartPanel(JFreeChart chart) {
        ChartPanel panel = new ChartPanel(chart);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private JFreeChart createMakespanComparisonChart(List<ComparisonResult> results) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (ComparisonResult r : results) {
            String cat = r.taskCount + " tasks";
            ds.addValue(r.cpopMetrics.getMakespan(), "CPOP", cat);
            ds.addValue(r.psoMetrics.getMakespan(), "PSO", cat);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Makespan Comparison",
                "Number of Tasks",
                "Makespan (seconds)",
                ds,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChart(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        styleBarPlot(plot, CPOP_COLOR, PSO_COLOR);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setMaximumBarWidth(0.15);
        renderer.setItemMargin(0.1);

        return chart;
    }

    private JFreeChart createEnergyComparisonChart(List<ComparisonResult> results) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (ComparisonResult r : results) {
            String cat = r.taskCount + " tasks";
            ds.addValue(r.cpopMetrics.getTotalEnergyConsumption(), "CPOP", cat);
            ds.addValue(r.psoMetrics.getTotalEnergyConsumption(), "PSO", cat);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Energy Consumption Comparison",
                "Number of Tasks",
                "Energy Consumption (Joules)",
                ds,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChart(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        styleBarPlot(plot, CHART_COLORS[0], CHART_COLORS[1]);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setMaximumBarWidth(0.15);
        renderer.setItemMargin(0.1);

        return chart;
    }

    private JFreeChart createQoSComparisonChart(List<ComparisonResult> results) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (ComparisonResult r : results) {
            String cat = r.taskCount + " tasks";
            ds.addValue(r.cpopMetrics.getQoS() * 100, "CPOP", cat);
            ds.addValue(r.psoMetrics.getQoS() * 100, "PSO", cat);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Quality of Service (QoS) Comparison",
                "Number of Tasks",
                "QoS Achievement (%)",
                ds,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChart(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        styleBarPlot(plot, CHART_COLORS[2], CHART_COLORS[3]);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setMaximumBarWidth(0.15);
        renderer.setItemMargin(0.1);

        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setRange(0, 105);

        return chart;
    }

    private JFreeChart createScalabilityChart(List<ComparisonResult> results) {
        XYSeriesCollection ds = new XYSeriesCollection();
        XYSeries s1 = new XYSeries("CPOP");
        XYSeries s2 = new XYSeries("PSO");
        for (ComparisonResult r : results) {
            s1.add(r.taskCount, r.cpopMetrics.getSchedulingTime());
            s2.add(r.taskCount, r.psoMetrics.getSchedulingTime());
        }
        ds.addSeries(s1);
        ds.addSeries(s2);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Algorithm Scalability Analysis",
                "Number of Tasks",
                "Scheduling Time (milliseconds)",
                ds,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChart(chart);

        XYPlot plot = chart.getXYPlot();
        styleXYPlot(plot);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);

        renderer.setSeriesPaint(0, CPOP_COLOR);
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesShape(0, new Ellipse2D.Double(-4, -4, 8, 8));
        renderer.setSeriesFillPaint(0, CPOP_COLOR);
        renderer.setSeriesShapesFilled(0, true);

        renderer.setSeriesPaint(1, PSO_COLOR);
        renderer.setSeriesStroke(1, new BasicStroke(2.5f));
        Shape diamond = createDiamond(5f);
        renderer.setSeriesShape(1, diamond);
        renderer.setSeriesFillPaint(1, PSO_COLOR);
        renderer.setSeriesShapesFilled(1, true);

        plot.setRenderer(renderer);

        return chart;
    }

    private void customizeChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);

        if (chart.getTitle() != null) {
            chart.getTitle().setFont(TITLE_FONT);
            chart.getTitle().setPadding(new RectangleInsets(10, 0, 10, 0));
        }

        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(LEGEND_FONT);
            chart.getLegend().setPosition(RectangleEdge.BOTTOM);
            chart.getLegend().setFrame(BlockBorder.NONE);
            chart.getLegend().setBackgroundPaint(Color.WHITE);
            chart.getLegend().setItemLabelPadding(new RectangleInsets(5, 5, 5, 5));
        }

        chart.setPadding(new RectangleInsets(10, 10, 10, 10));
    }

    private void styleBarPlot(CategoryPlot plot, Color color1, Color color2) {
        plot.setBackgroundPaint(BACKGROUND_COLOR);
        plot.setDomainGridlinePaint(GRID_COLOR);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setDomainGridlineStroke(new BasicStroke(0.5f));
        plot.setRangeGridlineStroke(new BasicStroke(0.5f));
        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinesVisible(false);

        plot.setOutlineVisible(true);
        plot.setOutlineStroke(new BasicStroke(1.0f));
        plot.setOutlinePaint(new Color(180, 180, 180));

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLabelFont(AXIS_LABEL_FONT);
        domainAxis.setTickLabelFont(TICK_FONT);
        domainAxis.setAxisLinePaint(Color.BLACK);
        domainAxis.setTickMarkPaint(Color.BLACK);
        domainAxis.setCategoryMargin(0.2);

        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setLabelFont(AXIS_LABEL_FONT);
        rangeAxis.setTickLabelFont(TICK_FONT);
        rangeAxis.setAxisLinePaint(Color.BLACK);
        rangeAxis.setTickMarkPaint(Color.BLACK);

        if (rangeAxis instanceof NumberAxis) {
            ((NumberAxis) rangeAxis).setAutoRangeIncludesZero(true);
        }

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, color1);
        renderer.setSeriesPaint(1, color2);
        renderer.setDrawBarOutline(true);
        renderer.setSeriesOutlinePaint(0, color1.darker());
        renderer.setSeriesOutlinePaint(1, color2.darker());
        renderer.setSeriesOutlineStroke(0, new BasicStroke(1.0f));
        renderer.setSeriesOutlineStroke(1, new BasicStroke(1.0f));
        renderer.setShadowVisible(false);

        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(false);
        renderer.setDefaultItemLabelFont(new Font("Arial", Font.PLAIN, 10));
    }

    private void styleXYPlot(XYPlot plot) {
        plot.setBackgroundPaint(BACKGROUND_COLOR);
        plot.setDomainGridlinePaint(GRID_COLOR);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setDomainGridlineStroke(new BasicStroke(0.5f));
        plot.setRangeGridlineStroke(new BasicStroke(0.5f));

        plot.setOutlineVisible(true);
        plot.setOutlineStroke(new BasicStroke(1.0f));
        plot.setOutlinePaint(new Color(180, 180, 180));

        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setLabelFont(AXIS_LABEL_FONT);
        xAxis.setTickLabelFont(TICK_FONT);
        xAxis.setAxisLinePaint(Color.BLACK);
        xAxis.setTickMarkPaint(Color.BLACK);
        xAxis.setAutoRangeIncludesZero(false);

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setLabelFont(AXIS_LABEL_FONT);
        yAxis.setTickLabelFont(TICK_FONT);
        yAxis.setAxisLinePaint(Color.BLACK);
        yAxis.setTickMarkPaint(Color.BLACK);
        yAxis.setAutoRangeIncludesZero(true);
    }

    private Shape createDiamond(float size) {
        GeneralPath path = new GeneralPath();
        path.moveTo(0f, -size);
        path.lineTo(size, 0f);
        path.lineTo(0f, size);
        path.lineTo(-size, 0f);
        path.closePath();
        return path;
    }

    private void saveCharts(JFreeChart... charts) {
        String[] names = {"makespan_comparison", "energy_comparison", "qos_comparison", "scalability_analysis"};
        try {
            File dir = new File("results/charts");
            if (!dir.exists()) dir.mkdirs();

            for (int i = 0; i < charts.length && i < names.length; i++) {
                File pngFile = new File(dir, names[i] + "_ieee.png");
                ChartUtils.saveChartAsPNG(pngFile, charts[i], 800, 600, null, true, 0);
                System.out.println("Saved high-quality IEEE-style chart to: " + pngFile.getAbsolutePath());

                File highResPng = new File(dir, names[i] + "_ieee_hires.png");
                ChartUtils.saveChartAsPNG(highResPng, charts[i], 1600, 1200, null, true, 0);
                System.out.println("Saved high-resolution chart to: " + highResPng.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error saving charts: " + e.getMessage());
        }
    }
}
