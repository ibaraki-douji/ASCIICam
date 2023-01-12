package fr.ibaraki.asciicam;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.VideoInputFrameGrabber;

public class Main {

	public static int height = 60;
	public static int fps = 4;
	public static int streamSource = 0;
	public static String file = null;
	public static final String ASCIIdic = " .:-=+*#%@▓";

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		try {
			if (Arrays.asList(args).indexOf("--list") != -1) {
				System.out.println(String.join("\n\n", VideoInputFrameGrabber.getDeviceDescriptions()));
				return;
			}
		} catch (Exception e) {}
		try {
			Main.fps = Integer.parseInt(args[Arrays.asList(args).indexOf("--fps")+1]);
		} catch (Exception e) {}
		
		try {
			Main.height = Integer.parseInt(args[Arrays.asList(args).indexOf("--height")+1]);
		} catch (Exception e) {}
		
		try {
			Main.streamSource = Integer.parseInt(args[Arrays.asList(args).indexOf("--source")+1]);
		} catch (Exception e) {}
		
		try {
			Main.file = args[Arrays.asList(args).indexOf("--file")+1];
			if (Arrays.asList(args).indexOf("--file") == -1) Main.file = null;
		} catch (Exception e) {
			Main.file = null;
		}
		
		if (Main.file != null) {
			if (!(
					Main.file.endsWith("jpg") ||
					Main.file.endsWith("png") ||
					Main.file.endsWith("jpeg")
				)) {
				System.out.println("You can only use image for the file path");
				return;
			};
			BufferedImage o = ImageIO.read(new File(Main.file));
			BufferedImage i = Main.resize(o, (int)(Main.keepRatioForWidth(o.getWidth(), o.getHeight(), height)*(float)((float)5/(float)2)), Main.height);
			Main.process(i);
			return;
		}
		
		
		FrameGrabber grabber = FrameGrabber.createDefault(Main.streamSource);
		Java2DFrameConverter bimConverter = new Java2DFrameConverter();
		grabber.setFormat("gdigrab");
		grabber.setFrameRate(60);
		grabber.start();
        
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					while (true) {
						Thread.sleep(1000 / Main.fps);
						
						Frame f = grabber.grab();
						BufferedImage o = bimConverter.convert(f);
						BufferedImage i = Main.resize(o, (int)(Main.keepRatioForWidth(o.getWidth(), o.getHeight(), height)*(float)((float)5/(float)2)), Main.height);
						Main.process(i);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
        
	}
	
	public static void process(BufferedImage i) {
		String end = "";
		for (int y = 0; y < i.getHeight(); y++) {
			for (int x = 0; x < i.getWidth(); x++) {
				int color = i.getRGB(x, y);
				int blue = color & 0xff;
				int green = (color & 0xff00) >> 8;
				int red = (color & 0xff0000) >> 16;
				end += Main.getASCII(red, green, blue);
			}
			end += "\n";
		}
		System.out.print(end.substring(0, end.length()-1));
	}
	
	public static String getASCII(int r, int g, int b) {
		int gray = (r+g+b)/3;
		try {
			return Main.ASCIIdic.split("")[(int) Math.floor(gray * Main.ASCIIdic.split("").length / 255)];
		} catch (ArrayIndexOutOfBoundsException e) {
			return Main.ASCIIdic.split("")[Main.ASCIIdic.split("").length-1];
		}
	}
	
	public static BufferedImage resize(BufferedImage img, int width, int height) {
		BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    Graphics2D graphics2D = resizedImage.createGraphics();
	    graphics2D.drawImage(img, 0, 0, width, height, null);
	    graphics2D.dispose();
	    return resizedImage;
	}
	
	public static int keepRatioForWidth(int originalW, int originalH, int newH) {
		return (int)(((float)((float)originalW / (float)originalH)) * newH);
	}

}
