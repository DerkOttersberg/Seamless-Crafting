package com.derk.seamlesscrafting.client;

import java.util.Locale;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class SeamlessColorPickerScreen extends Screen {
	private final Screen parent;
	private final IntConsumer onSave;
	private int color;
	private TextFieldWidget hexField;
	private RgbSliderWidget redSlider;
	private RgbSliderWidget greenSlider;
	private RgbSliderWidget blueSlider;
	private Text errorText = Text.empty();
	private boolean syncingControls;

	public SeamlessColorPickerScreen(Screen parent, int initialColor, IntConsumer onSave) {
		super(Text.of("Highlight Color"));
		this.parent = parent;
		this.onSave = onSave;
		this.color = initialColor & 0xFFFFFF;
	}

	@Override
	protected void init() {
		int centerX = this.width / 2;
		int panelX = centerX - 140;
		int fieldX = panelX + 140;
		int rowY = 68;

		this.hexField = new TextFieldWidget(this.textRenderer, fieldX, rowY - 2, 120, 20, Text.empty());
		this.hexField.setText(this.derk$formatHex(this.color));
		this.addDrawableChild(this.hexField);

		this.redSlider = this.addDrawableChild(new RgbSliderWidget(panelX + 92, rowY + 34, 168, 20, "Red", (this.color >> 16) & 0xFF, value -> this.derk$updateColorFromSliders()));
		this.greenSlider = this.addDrawableChild(new RgbSliderWidget(panelX + 92, rowY + 64, 168, 20, "Green", (this.color >> 8) & 0xFF, value -> this.derk$updateColorFromSliders()));
		this.blueSlider = this.addDrawableChild(new RgbSliderWidget(panelX + 92, rowY + 94, 168, 20, "Blue", this.color & 0xFF, value -> this.derk$updateColorFromSliders()));

		this.addDrawableChild(ButtonWidget.builder(Text.of("Apply Hex"), button -> this.derk$applyHex())
				.dimensions(fieldX + 126, rowY - 2, 80, 20)
				.build());
		this.addDrawableChild(ButtonWidget.builder(Text.of("Cancel"), button -> this.close())
				.dimensions(centerX - 104, this.height - 52, 100, 20)
				.build());
		this.addDrawableChild(ButtonWidget.builder(Text.of("Save"), button -> {
			this.onSave.accept(this.color);
			this.close();
		})
				.dimensions(centerX + 4, this.height - 52, 100, 20)
				.build());

		this.derk$syncControlsFromColor();
	}

	@Override
	public void close() {
		this.client.setScreen(this.parent);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, this.width, this.height, 0xD0101014);
		int centerX = this.width / 2;
		int panelX = centerX - 140;
		int panelY = 36;
		int panelWidth = 280;
		int panelHeight = 170;
		context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xAA1A1D24);
		this.derk$drawBorder(context, panelX, panelY, panelWidth, panelHeight, 0xFF444A56);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, panelY + 14, 0xFFFFFF);

		context.drawTextWithShadow(this.textRenderer, Text.of("Hex Code"), panelX + 18, panelY + 34, 0xE6E6E6);
		context.drawTextWithShadow(this.textRenderer, Text.of("Preview"), panelX + 18, panelY + 68, 0xE6E6E6);
		context.fill(panelX + 18, panelY + 82, panelX + 70, panelY + 134, 0xFF000000 | this.color);
		this.derk$drawBorder(context, panelX + 18, panelY + 82, 52, 52, 0xFFFFFFFF);
		context.drawTextWithShadow(this.textRenderer, Text.of(this.derk$formatHex(this.color)), panelX + 82, panelY + 86, 0xFFFFFF);
		context.drawTextWithShadow(this.textRenderer, Text.of("Use the sliders or type a hex value."), panelX + 82, panelY + 102, 0xB7BDC9);

		if (!this.errorText.getString().isEmpty()) {
			context.drawCenteredTextWithShadow(this.textRenderer, this.errorText, centerX, this.height - 78, 0xFF6B6B);
		}

		super.render(context, mouseX, mouseY, delta);
	}

	private void derk$applyHex() {
		try {
			this.color = this.derk$parseHexColor(this.hexField.getText());
			this.derk$syncControlsFromColor();
			this.errorText = Text.empty();
		} catch (IllegalArgumentException exception) {
			this.errorText = Text.of(exception.getMessage());
		}
	}

	private void derk$updateColorFromSliders() {
		if (this.syncingControls) {
			return;
		}
		this.color = (this.redSlider.getChannelValue() << 16) | (this.greenSlider.getChannelValue() << 8) | this.blueSlider.getChannelValue();
		this.hexField.setText(this.derk$formatHex(this.color));
		this.errorText = Text.empty();
	}

	private void derk$syncControlsFromColor() {
		this.syncingControls = true;
		this.hexField.setText(this.derk$formatHex(this.color));
		this.redSlider.setChannelValue((this.color >> 16) & 0xFF);
		this.greenSlider.setChannelValue((this.color >> 8) & 0xFF);
		this.blueSlider.setChannelValue(this.color & 0xFF);
		this.syncingControls = false;
	}

	private int derk$parseHexColor(String raw) {
		String normalized = raw.trim();
		if (normalized.startsWith("#")) {
			normalized = normalized.substring(1);
		}
		if (!normalized.matches("[0-9a-fA-F]{6}")) {
			throw new IllegalArgumentException("Color must be a 6-digit hex value.");
		}
		return Integer.parseInt(normalized, 16);
	}

	private String derk$formatHex(int colorValue) {
		return String.format(Locale.ROOT, "#%06X", colorValue & 0xFFFFFF);
	}

	private void derk$drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + 1, color);
		context.fill(x, y + height - 1, x + width, y + height, color);
		context.fill(x, y, x + 1, y + height, color);
		context.fill(x + width - 1, y, x + width, y + height, color);
	}

	private static final class RgbSliderWidget extends SliderWidget {
		private final String label;
		private final java.util.function.IntConsumer onValueChanged;

		private RgbSliderWidget(int x, int y, int width, int height, String label, int initialValue, java.util.function.IntConsumer onValueChanged) {
			super(x, y, width, height, Text.empty(), initialValue / 255.0);
			this.label = label;
			this.onValueChanged = onValueChanged;
			this.updateMessage();
		}

		private int getChannelValue() {
			return (int)Math.round(this.value * 255.0);
		}

		private void setChannelValue(int value) {
			this.value = Math.max(0.0, Math.min(1.0, value / 255.0));
			this.updateMessage();
		}

		@Override
		protected void updateMessage() {
			this.setMessage(Text.of(this.label + ": " + this.getChannelValue()));
		}

		@Override
		protected void applyValue() {
			this.onValueChanged.accept(this.getChannelValue());
		}
	}
}

