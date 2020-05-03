package org.gitremoteurl;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.lang.StringUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.loaders.DataObject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@ActionID(
		category = "Git",
		id = "org.gitremoteurl.openRemoteUrl"
)
@ActionRegistration(
		displayName = "#CTL_openRemoteUrl"
)

@ActionReferences({
	@ActionReference(path = "Loaders/folder/any/Actions", position = 1500),
	@ActionReference(path = "Projects/Actions"),
	@ActionReference(path = "Editors/Popup")
})

@Messages("CTL_openRemoteUrl=Open Remote Git")
public final class openRemoteUrl implements ActionListener {

	private final DataObject context;

	public openRemoteUrl(DataObject context) {
		this.context = context;
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		FileObject contextNode = context.getPrimaryFile();
		String contextNodePath = contextNode.getPath();
		FileObject dataLookup = TopComponent.getRegistry().getActivated().getLookup()
				.lookup(FileObject.class);
		if (dataLookup != null) {
			FileObject projectDirectory = FileOwnerQuery.getOwner(dataLookup).getProjectDirectory();
			String projectDir = projectDirectory.getPath();
			String projectDirName = projectDirectory.getName();
			if (!"".equals(projectDir)) {
				File gitConfig = new File(projectDir + File.separator + ".git" + File.separator + "config");
				if (gitConfig.exists()) {
					try (BufferedReader reader = new BufferedReader(new FileReader(gitConfig))) {
						String line = null;
						while ((line = reader.readLine()) != null) {
							if (StringUtils.containsIgnoreCase(line, "url")) {
								String url = line.split("=")[1];
								if (!"".equals(url) && Desktop.isDesktopSupported()
										&& Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
									if (contextNodePath.equals(projectDir)) {
										Desktop.getDesktop().browse(new URI(url));
										break;
									}
									if (StringUtils.containsIgnoreCase(url, "github.")) {
										if (!contextNodePath.equals(projectDir) & contextNode.isFolder()) {
											String folderToAdd = StringUtils.removeStart(contextNodePath, projectDir);
											url = url.replace(".git", "/tree/master" + folderToAdd);
											Desktop.getDesktop().browse(new URI(url));
											break;
										}
										if (!contextNodePath.equals(projectDir) & !contextNode.isFolder()) {
											String fileToAdd = StringUtils.removeStart(contextNodePath, projectDir);
											url = url.replace(".git", "/blob/master" + fileToAdd);
											Desktop.getDesktop().browse(new URI(url));
										}
									}
								}
							}
						}

					} catch (URISyntaxException | IOException ex) {
						Exceptions.printStackTrace(ex);
					}
				} else {
					StatusDisplayer.getDefault().setStatusText("Missing git config file, "
							+ "Please make sure your git is well configure");
				}
			} else {
				StatusDisplayer.getDefault().setStatusText("Unable to find the project root directory");
			}
		}
	}
}
