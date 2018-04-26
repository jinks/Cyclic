/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (C) 2014-2018 Sam Bassett (aka Lothrazar)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.lothrazar.cyclicmagic.gui.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import com.lothrazar.cyclicmagic.core.block.TileEntityBaseMachineInvo;
import com.lothrazar.cyclicmagic.core.util.Const;
import com.lothrazar.cyclicmagic.core.util.Const.ScreenSize;
import com.lothrazar.cyclicmagic.core.util.UtilChat;
import com.lothrazar.cyclicmagic.gui.EnergyBar;
import com.lothrazar.cyclicmagic.gui.ITooltipButton;
import com.lothrazar.cyclicmagic.gui.ProgressBar;
import com.lothrazar.cyclicmagic.gui.button.GuiButtonTogglePreview;
import com.lothrazar.cyclicmagic.gui.button.GuiButtonToggleRedstone;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class GuiBaseContainer extends GuiContainer {

  public final static int FONTCOLOR = 4210752;
  public TileEntityBaseMachineInvo tile;
  private Const.ScreenSize screenSize = ScreenSize.STANDARD;
  protected int fieldRedstoneBtn = -1;
  protected int fieldPreviewBtn = -1;
  protected ArrayList<GuiTextField> txtBoxes = new ArrayList<GuiTextField>();
  public ArrayList<ButtonTriggerWrapper> buttonWrappers = new ArrayList<ButtonTriggerWrapper>();
  public ProgressBar progressBar = null;
  public EnergyBar energyBar = null;
  private GuiButtonToggleRedstone redstoneBtn = null;
  private GuiButtonTogglePreview btnPreview;
  protected int fuelX, fuelY, fuelXE, fuelYE;

  public GuiBaseContainer(Container inventorySlotsIn, TileEntityBaseMachineInvo tile) {
    super(inventorySlotsIn);
    this.tile = tile;
  }

  public GuiBaseContainer(Container inventorySlotsIn) {
    super(inventorySlotsIn);
    this.tile = null;
  }

  protected void setScreenSize(Const.ScreenSize ss) {
    this.screenSize = ss;
    this.xSize = screenSize.width();
    this.ySize = screenSize.height();
  }

  @Override
  public void initGui() {
    super.initGui();
    int x = this.guiLeft + Const.PAD / 2;
    int y = this.guiTop + Const.PAD / 2;
    if (this.fieldRedstoneBtn >= 0) {
      redstoneBtn = new GuiButtonToggleRedstone(1,
          x,
          y, this.tile.getPos());
      this.buttonList.add(redstoneBtn);
      y += Const.PAD / 2 + redstoneBtn.width;
    }
    if (this.fieldPreviewBtn > 0) {
      btnPreview = new GuiButtonTogglePreview(2,
          x,
          y, this.tile.getPos());
      this.buttonList.add(btnPreview);
    }

  }

  /**
   * ONLY CALL FROM drawGuiContainerForegroundLayer
   * 
   * @param x
   * @param y
   * @param f
   */
  protected void drawFieldAt(int x, int y, int f) {
    String display = "" + this.tile.getField(f);
    x = (display.length() > 1) ? x - 3 : x;
    this.drawString(display, x, y);
  }

  @SideOnly(Side.CLIENT)
  @Override
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    drawNameText();
    updateToggleButtonStates();

    for (GuiTextField txt : txtBoxes) {
      if (txt != null) {
        txt.drawTextBox();
      }
    }
  }

  /**
   * shift the x param over if the length is over 1, to center between the two digits made for numeric strings up to 99
   * 
   * @param display
   * @param x
   * @param y
   */
  public void drawStringCenteredCheckLength(String display, int x, int y) {
    x = (display.length() > 1) ? x - 3 : x;
    this.drawString(display, x, y);
  }

  public void drawNameText() {
    if (tile != null) {
      String s = UtilChat.lang(tile.getName());
      this.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6);
    }
  }

  public void updateToggleButtonStates() {
    if (redstoneBtn != null) {
      redstoneBtn.setState(tile.getField(this.fieldRedstoneBtn));
    }
    if (btnPreview != null) {
      if (tile.getField(this.fieldPreviewBtn) == 1) {
        btnPreview.setStateOn();
      }
      else {
        btnPreview.setStateOff();
      }
    }
    updateDisabledButtonTriggers();
  }

  public void drawStringCentered(String s, int x, int y) {
    this.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, y);
  }

  public void drawString(String s, int x, int y) {
    this.fontRenderer.drawString(UtilChat.lang(s), x, y, FONTCOLOR);
  }

  public int getMiddleY() {
    int yMiddle = (this.height - this.ySize) / 2;
    return yMiddle;
  }

  public int getMiddleX() {
    int xMiddle = (this.width - this.xSize) / 2;
    return xMiddle;
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    //      super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);// abstract
    this.drawDefaultBackground();//dim the background as normal
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    this.mc.getTextureManager().bindTexture(getScreenSize().texture());
    int thisX = getMiddleX();
    int thisY = getMiddleY();
    int u = 0, v = 0;
    Gui.drawModalRectWithCustomSizedTexture(thisX, thisY, u, v,
        getScreenSize().width(), getScreenSize().height(),
        getScreenSize().width(), getScreenSize().height());
    if (this.progressBar != null) {
      drawProgressBar();
    }
    if (tile == null) {
      return;
    }
    //    if (this.fieldFuel > -1 && tile != null && tile.doesUseFuel()) {
    //      //this.btnFuelToggle
    //    }
    if (this.energyBar != null && tile.hasCapability(CapabilityEnergy.ENERGY, EnumFacing.UP)) {
      this.drawEnergyBar();
    }

  }

  private void drawEnergyBar() {
    int u = 0, v = 0;
    IEnergyStorage energy = tile.getCapability(CapabilityEnergy.ENERGY, EnumFacing.UP);
    float percent = ((float) energy.getEnergyStored()) / ((float) energy.getMaxEnergyStored());
    //must store fuelXY for tooltip?
    int outerLength = energyBar.getHeight() + 2 * energyBar.getBorder();
    int outerWidth = energyBar.getWidth() + 2 * energyBar.getBorder();
    //draw the outer container
    this.mc.getTextureManager().bindTexture(Const.Res.ENERGY_CTR);
    Gui.drawModalRectWithCustomSizedTexture(
        this.guiLeft + energyBar.getX(),
        this.guiTop + energyBar.getY(), u, v,
        outerWidth, outerLength,
        outerWidth, outerLength);
    //draw the inner actual thing
    this.mc.getTextureManager().bindTexture(Const.Res.ENERGY_INNER);
    Gui.drawModalRectWithCustomSizedTexture(
        this.guiLeft + energyBar.getX() + energyBar.getBorder(),
        this.guiTop + energyBar.getY() + energyBar.getBorder(), u, v,
        energyBar.getWidth(), (int) (energyBar.getHeight() * percent),
        energyBar.getWidth(), energyBar.getHeight());
  }


  private String getFuelAmtDisplay() {
    IEnergyStorage energy = tile.getCapability(CapabilityEnergy.ENERGY, EnumFacing.UP);
    return energy.getEnergyStored() + "/" + energy.getMaxEnergyStored();
  }

  private void renderEnergyTooltip(int mouseX, int mouseY) {
    String display = getFuelAmtDisplay();
    drawHoveringText(Arrays.asList(display), mouseX, mouseY, fontRenderer);
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    super.drawScreen(mouseX, mouseY, partialTicks);
    this.renderHoveredToolTip(mouseX, mouseY);
    if (energyBar != null && energyBar.isMouseover(mouseX, mouseY)) {
      this.renderEnergyTooltip(mouseX, mouseY);
    }
    ITooltipButton btn;
    for (int i = 0; i < buttonList.size(); i++) {
      if (buttonList.get(i).isMouseOver() && buttonList.get(i) instanceof ITooltipButton) {
        btn = (ITooltipButton) buttonList.get(i);
        if (btn.getTooltips() != null) {
          drawHoveringText(btn.getTooltips(), mouseX, mouseY);
        }
        break;// cant hover on 2 at once
      }
    }
  }

  public void drawProgressBar() {
    int u = 0, v = 0;
    this.mc.getTextureManager().bindTexture(progressBar.getProgressCtrAsset());
    Gui.drawModalRectWithCustomSizedTexture(
        this.guiLeft + progressBar.xOffset,
        this.guiTop + progressBar.yOffset, u, v,
        ProgressBar.WIDTH, ProgressBar.HEIGHT,
        ProgressBar.WIDTH, ProgressBar.HEIGHT);
    if (progressBar.getProgressCurrent() > 0) {
      this.mc.getTextureManager().bindTexture(progressBar.getProgressAsset());
      float percent = ((float) progressBar.getProgressCurrent()) / ((float) progressBar.maxValue);
      Gui.drawModalRectWithCustomSizedTexture(
          this.guiLeft + progressBar.xOffset,
          this.guiTop + progressBar.yOffset,
          u, v,
          (int) (ProgressBar.WIDTH * percent),
          ProgressBar.HEIGHT, ProgressBar.WIDTH, ProgressBar.HEIGHT);
    }
  }

  @Override
  protected <T extends GuiButton> T addButton(T buttonIn) {
    return super.addButton(buttonIn);
  }

  protected void registerButtonDisableTrigger(GuiButton buttonIn, ButtonTriggerWrapper.ButtonTriggerType trigger,
      int fieldId, int fv) {
    this.buttonWrappers.add(new ButtonTriggerWrapper(buttonIn, trigger, fieldId, fv));
  }

  private void updateDisabledButtonTriggers() {
    for (ButtonTriggerWrapper btnWrap : this.buttonWrappers) {
      int fieldValue = this.tile.getField(btnWrap.fld);
      boolean isDisabled = true;
      switch (btnWrap.trig) {
        case EQUAL:
          isDisabled = (fieldValue == btnWrap.triggerValue);
        break;
        case GREATER:
          isDisabled = (fieldValue > btnWrap.triggerValue);
        break;
        case LESS:
          isDisabled = (fieldValue < btnWrap.triggerValue);
        break;
        case NOTEQUAL:
          isDisabled = (fieldValue != btnWrap.triggerValue);
        break;
      }
      btnWrap.btn.enabled = !isDisabled;
    }
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int btn) throws IOException {
    super.mouseClicked(mouseX, mouseY, btn);// x/y pos is 33/30
    for (GuiTextField txt : txtBoxes) {
      txt.mouseClicked(mouseX, mouseY, btn);
      if (btn == 0) {//basically left click
        boolean flag = mouseX >= this.guiLeft + txt.x && mouseX < this.guiLeft + txt.x + txt.width
            && mouseY >= this.guiTop + txt.y && mouseY < this.guiTop + txt.y + txt.height;
        txt.setFocused(flag);
      }
    }
  }

  protected Const.ScreenSize getScreenSize() {
    return screenSize;
  }

  public static class ButtonTriggerWrapper {

    public static enum ButtonTriggerType {
      GREATER, LESS, EQUAL, NOTEQUAL;
    }

    public GuiButton btn;
    public ButtonTriggerType trig;
    public int fld;
    public int triggerValue;

    public ButtonTriggerWrapper(GuiButton buttonIn, ButtonTriggerType trigger, int fieldId, int tval) {
      this.btn = buttonIn;
      this.trig = trigger;
      this.fld = fieldId;
      this.triggerValue = tval;
    }
  }
}
