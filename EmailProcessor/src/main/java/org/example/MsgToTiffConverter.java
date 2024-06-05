private static BufferedImage renderTextToImage(String text) {
        int width = 800;
        int height = 1000;
        int padding = 20;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setPaint(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        g2d.setPaint(Color.BLACK);
        g2d.setFont(new Font("Serif", Font.PLAIN, 14));

        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight();
        int y = padding + lineHeight;
        int maxLineWidth = width - 2 * padding;

        for (String line : text.split("\n")) {
            while (line.length() > 0) {
                int len = fm.stringWidth(line) <= maxLineWidth ? line.length() : findWrapPosition(line, fm, maxLineWidth);
                g2d.drawString(line.substring(0, len), padding, y);
                line = line.substring(len).trim();
                y += lineHeight;
            }
        }

        g2d.dispose();
        return image;
    }
