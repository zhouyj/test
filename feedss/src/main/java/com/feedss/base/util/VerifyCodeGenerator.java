package com.feedss.base.util;

import static org.apache.commons.lang3.RandomUtils.nextInt;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.apache.commons.lang.RandomStringUtils;

/**
 * 验证码生成器
 *
 */
public class VerifyCodeGenerator {
	/**
	 * 验证码图片的宽度
	 */
	private int width;
	/**
	 * 验证码图片的高度
	 */
	private int height;
	/**
	 * 验证码
	 */
	private String code;

	/**
	 * @param code
	 * @param width
	 * @param height
	 */
	public VerifyCodeGenerator(String code, int width, int height) {
		this.code = code;
		this.width = width;
		this.height = height;
	}

	/**
	 * 随机码
	 *
	 * @param code
	 */
	public VerifyCodeGenerator(String code) {
		this(code, 80, 60);
	}

	/**
	 * 生成图片
	 *
	 * @return
	 */
	public BufferedImage gen() {
		// 定义图像buffer
		BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = buffImg.createGraphics();
		// 将图像填充为白色
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		// 创建字体，字体的大小应该根据图片的高度来定。
		Font font = new Font(null, Font.PLAIN, height * 2 / 3);
		// 设置字体。
		g.setFont(font);
		// 画边框。
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, width, height);
		// 随机产生160条干扰线，使图象中的认证码不易被其它程序探测到。
		g.setColor(Color.BLACK);
		for (int i = 0; i < 20; i++) {
			int x1 = nextInt(0, width);
			int y1 = nextInt(0, height);
			int x2 = nextInt(0, width);
			int y2 = nextInt(0, height);
			g.drawLine(x1, y1, x2, y2);
		}
		// randomCode用于保存随机产生的验证码，以便用户登录后进行验证。
		int x = width / (code.length() + 1);
		for (int i = 0; i < code.length(); i++) {
			String str = code.substring(i, i + 1);
			// 产生随机的颜色分量来构造颜色值，这样输出的每位数字的颜色值都将不同。
			g.setColor(new Color(nextInt(0, 255), nextInt(0, 255), nextInt(0, 255)));
			g.drawString(str, (i + 0.5f) * x, height * 5 / 6);
		}
		return buffImg;
	}

	public String getCode() {
		return code;
	}

	public static VerifyCodeGenerator random() {
		return new VerifyCodeGenerator(RandomStringUtils.randomAlphanumeric(4));
	}
}
