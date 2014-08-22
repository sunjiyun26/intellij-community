package org.jetbrains.plugins.coursecreator.ui;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.coursecreator.format.TaskWindow;

import javax.swing.*;
import java.io.*;

public class CreateTaskWindowDialog extends DialogWrapper {

  public static final String TITLE = "New Task Window";
  private static final Logger LOG = Logger.getInstance(CreateTaskWindowDialog.class.getName());
  private final TaskWindow myTaskWindow;
  private final CreateTaskWindowPanel myPanel;
  private final Project myProject;

  public CreateTaskWindowDialog(@NotNull final Project project, @NotNull final TaskWindow taskWindow) {
    super(project, true);
    setTitle(TITLE);
    myTaskWindow = taskWindow;
    myPanel = new CreateTaskWindowPanel();
    if (taskWindow.getHintName() != null) {
      setHintText(project, taskWindow);
    }
    myProject = project;
    String taskWindowTaskText = taskWindow.getTaskText();
    myPanel.setTaskWindowText(taskWindowTaskText != null ? taskWindowTaskText : "");
    String hintName = taskWindow.getHintName();
    myPanel.setHintName(hintName != null ? hintName : "");
    init();
  }

  private void setHintText(Project project, TaskWindow taskWindow) {
    VirtualFile hints = project.getBaseDir().findChild("hints");
    if (hints != null) {
      File file = new File(hints.getPath(), taskWindow.getHintName());
      StringBuilder hintText = new StringBuilder();
      if (file.exists()) {
        try {
          BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
          String line;
          while ((line = bufferedReader.readLine()) != null) {
            hintText.append(line).append("\n");
          }
          myPanel.setHintText(hintText.toString());
          myPanel.enableHint(true);
        }
        catch (FileNotFoundException e) {
          LOG.error("created hint was not found", e);
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  protected void doOKAction() {
    String taskWindowText = myPanel.getTaskWindowText();
    myTaskWindow.setTaskText(StringUtil.notNullize(taskWindowText));
    if (myPanel.createHint()) {
      String hintName = myPanel.getHintName();
      myTaskWindow.setHint(hintName);
      String hintText = myPanel.getHintText();
      createHint(hintName, hintText);
    }
    super.doOKAction();
  }

  private void createHint(String hintName, String hintText) {
    VirtualFile hintsDir = myProject.getBaseDir().findChild("hints");
    if (hintsDir != null) {
      File hintFile = new File(hintsDir.getPath(), hintName);
      PrintWriter printWriter = null;
      try {
        printWriter = new PrintWriter(hintFile);
        printWriter.print(hintText);
      }
      catch (FileNotFoundException e) {
        //TODO:show error in UI
        return;
      }
      finally {
        if (printWriter != null) {
          printWriter.close();
        }
      }
    }
    VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);
    ProjectView.getInstance(myProject).refresh();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return myPanel;
  }
}
