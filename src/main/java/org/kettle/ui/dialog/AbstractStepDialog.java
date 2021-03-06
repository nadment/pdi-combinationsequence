package org.kettle.ui.dialog;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.util.SwtSvgImageUtil;

public abstract class AbstractStepDialog<T extends StepMetaInterface> extends BaseStepDialog
    implements StepDialogInterface {

  public static final int LARGE_MARGIN = 15;

  protected static final int BUTTON_WIDTH = 80;

  protected ModifyListener lsMod;
  
  
  public AbstractStepDialog(Shell parent, Object stepMeta, TransMeta transMeta, String stepname) {
    super(parent, (StepMetaInterface) stepMeta, transMeta, stepname);

    //setText(stepMeta.getClass().getAnnotation(org.pentaho.di.core.annotations.Step.class).name());
  }

  @SuppressWarnings("unchecked")
  public T getStepMeta() {
    return (T) this.baseStepMeta;
  }

  protected final Control createContents(final Composite parent) {

    Control titleArea = this.createTitleArea(parent);

    // The title separator line
    Label titleSeparator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
    titleSeparator.setLayoutData(new FormDataBuilder().top(titleArea, LARGE_MARGIN).fullWidth().result());
    props.setLook(titleSeparator);

    // The button bar
    Control buttonBar = this.createButtonBar(parent);
    buttonBar.setLayoutData(new FormDataBuilder().fullWidth().bottom().result());

    // The bottom separator line
    Label bottomSeparator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
    bottomSeparator.setLayoutData(new FormDataBuilder().bottom(buttonBar, -LARGE_MARGIN).fullWidth().result());
    props.setLook(bottomSeparator);

    Composite area = new Composite(parent, SWT.NONE);
    area.setLayout(new FormLayout());
    area.setLayoutData(new FormDataBuilder().top(titleSeparator, LARGE_MARGIN).bottom(bottomSeparator, -LARGE_MARGIN)
        .fullWidth().result());
    props.setLook(area);

    this.createDialogArea(area);

    return area;
  }

  protected final Control createTitleArea(final Composite parent) {

    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new FormLayout());
    composite.setLayoutData(new FormDataBuilder().top().fullWidth().result());
    props.setLook(composite);

    Label icon = new Label(composite, SWT.CENTER);
    icon.setImage(getImage());
    icon.setLayoutData(new FormDataBuilder().top().right(100, 0).width(ConstUI.ICON_SIZE).result());
    props.setLook(icon);

    Label label = new Label(composite, SWT.NONE);
    label.setText(BaseMessages.getString("System.Label.StepName"));
    label.setLayoutData(new FormDataBuilder().top().left().right(icon, 100).result());
    props.setLook(label);
    
    // Widget Step name
    wStepname = new Text(composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setLayoutData(new FormDataBuilder().top(label).left().right(icon, -ConstUI.ICON_SIZE).result());
    wStepname.addModifyListener(lsMod);
    wStepname.addSelectionListener(lsDef);
    props.setLook(wStepname);
        
    final ControlDecoration deco = new ControlDecoration(wStepname, SWT.TOP | SWT.LEFT);
    deco.setDescriptionText(BaseMessages.getString("System.StepNameMissing.Msg"));
    deco.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage());
    deco.setShowOnlyOnFocus(true);
    deco.hide();
    
    wStepname.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        if (wStepname.getText().length() > 0) {
          deco.hide();
        } else {
          deco.show();
        }
        
        baseStepMeta.setChanged();
        
        wOK.setEnabled(isValid());
      }
    });
    
    return composite;
  }

  /**
   * Creates and returns the contents of the upper part of this dialog (above the button bar).
   * <p>
   * The <code>Dialog</code> implementation of this framework method creates and returns a new <code>Composite</code> with no margins and spacing. Subclasses
   * should override.
   * </p>
   * 
   * @param parent
   *            The parent composite to contain the dialog area
   * @return the dialog area control
   */
  protected Control createDialogArea(final Composite parent) {

    // Create the top level composite for the dialog area
    Composite composite = new Composite(parent, SWT.NONE);
    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;
    composite.setLayout(formLayout);

    return composite;
  }

  protected Control createButtonBar(final Composite parent) {

    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new FormLayout());
    composite.setLayoutData(new FormDataBuilder().fullWidth().bottom().result());
    composite.setFont(parent.getFont());
    props.setLook(composite);

    // Add the buttons to the button bar.
    this.createButtonsForButtonBar(composite);

    return composite;
  }

  protected void createButtonsForButtonBar(final Composite parent) {
    wCancel = new Button(parent, SWT.PUSH);
    wCancel.setText(BaseMessages.getString("System.Button.Cancel"));
//    wCancel.setLayoutData(new FormDataBuilder().bottom().right().width(BUTTON_WIDTH).result());
    wCancel.setLayoutData(new FormDataBuilder().bottom().right().result());
    wCancel.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event e) {
        onCancelPressed();
      }
    });

    wOK = new Button(parent, SWT.PUSH);
    wOK.setText(BaseMessages.getString("System.Button.OK"));
//    wOK.setLayoutData(new FormDataBuilder().bottom().right(wCancel, -ConstUI.SMALL_MARGIN).width(BUTTON_WIDTH).result());
    wOK.setLayoutData(new FormDataBuilder().bottom().right(wCancel, -ConstUI.SMALL_MARGIN).result());
    wOK.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event e) {
        onOkPressed();
      }
    });
  }

  /**
   * This method is called by Spoon when the user opens the settings dialog of the step. It opens the dialog and returns only once the dialog has been closed
   * by the user.
   *
   * If the user confirms the dialog, the meta object (passed in the constructor) is updated to reflect the new step settings. The changed flag of the meta
   * object reflect whether the step configuration was changed by the dialog.
   *
   * If the user cancels the dialog, the meta object is not updated
   *
   * The open() method returns the name of the step after the user has confirmed the dialog, or null if the user cancelled the dialog.
   */
  @Override
  public String open() {

    Shell parent = getParent();
    Display display = parent.getDisplay();

    // Create shell
    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
    shell.setText(getText());
    // desactiver pour les tests uniquement sinon NPE
    setShellImage(shell, baseStepMeta);

    //shell.setImage(GUIResource.getInstance().getImageVariable());

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = LARGE_MARGIN;
    formLayout.marginHeight = LARGE_MARGIN;
    shell.setLayout(formLayout);
    shell.setMinimumSize(getMinimumSize());
    props.setLook(shell);

    
    // Default listener (for hitting "enter")
    lsDef = new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        onOkPressed();
      }
    };
    
    // The ModifyListener used on all controls. It will update the meta object to
    // indicate that changes are being made.
    lsMod = new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        baseStepMeta.setChanged();
        
        wOK.setEnabled(isValid());
      }
    };
    
    
    this.createContents(shell);

    // Save the value of the changed flag on the meta object. If the user cancels
    // the dialog, it will be restored to this saved value.
    // The "changed" variable is inherited from BaseStepDialog
    changed = stepMeta.hasChanged();

    // Populate the dialog with the values from the meta object
    loadMeta(this.getStepMeta());

    // Restore the changed flag to original value, as the modify listeners fire during dialog population     
    stepMeta.setChanged(changed);



    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
      @Override
      public void shellClosed(ShellEvent e) {
        onCancelPressed();
      }
    });

    // Set/Restore the dialog size based on last position on screen
    setSize(shell);

    // Set focus on step name
    wStepname.setText(stepname);
    wStepname.selectAll();
    wStepname.setFocus();

    // Open dialog and enter event loop
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }

    // The "stepname" variable is inherited from BaseStepDialog
    return stepname;
  }

  public Image getImage() {

    PluginInterface plugin = PluginRegistry.getInstance().getPlugin(StepPluginType.class,
        stepMeta.getStepMetaInterface());

    if (plugin.getImageFile() != null) {
      return SwtSvgImageUtil.getImage(shell.getDisplay(), getClass().getClassLoader(), plugin.getImageFile(),
          ConstUI.ICON_SIZE, ConstUI.ICON_SIZE);
    }

    return GUIResource.getInstance().getImageStepError();

  }

  /**
   * Returns a point describing the minimum receiver's size. The x coordinate of the result is the minimum width of the receiver. The y coordinate of the
   * result is the minimum height of the receiver.
   * 
   * @return the receiver's size 
   */
  public Point getMinimumSize() {
    return new Point(100, 50);
  }

  /**
   * Called when the user confirms the dialog. Subclasses may override if desired.
   */
  protected void onOkPressed() {

    if (Utils.isEmpty(wStepname.getText())) {      
      return;
    }

    stepname = wStepname.getText();

    saveMeta(this.getStepMeta());

    // Close the SWT dialog window
    dispose();
  }

  /**
   * Called when the user cancels the dialog. Subclasses may override if desired.
   */
  protected void onCancelPressed() {
    stepname = null;

    // Restore initial state
    stepMeta.setChanged(changed);

    // Close the SWT dialog window
    dispose();
  }

  /**
  * This helper method takes the step configuration stored in the meta object and puts it into the dialog controls.
  */
  protected abstract void loadMeta(T stepMeta);

  /**
   * This helper method takes the information configured in the dialog controls and stores it into the step configuration meta object
   */
  protected abstract void saveMeta(T stepMeta);

  protected boolean isValid() {
    return !Utils.isEmpty(this.wStepname.getText());
  }
}
