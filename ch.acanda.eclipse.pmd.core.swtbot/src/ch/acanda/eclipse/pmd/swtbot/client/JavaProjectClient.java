// =====================================================================
//
// Copyright (C) 2012, Philip Graf
//
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// =====================================================================

package ch.acanda.eclipse.pmd.swtbot.client;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import ch.acanda.eclipse.pmd.swtbot.bot.PMDProjectPropertyDialogBot;

/**
 * @author Philip Graf
 */
public class JavaProjectClient {
    
    /**
     * Creates a Java project with the provided name.
     * 
     * @param name The name of the Java project.
     */
    public static void createJavaProject(final String name) {
        final SWTWorkbenchBot bot = new SWTWorkbenchBot();
        bot.menu("File").menu("New").menu("Project...").click();
        final SWTBotShell dialog = bot.shell("New Project");
        final SWTBot newProjectDialogBot = dialog.bot();
        final SWTBotTree projectTree = newProjectDialogBot.tree();
        projectTree.expandNode("Java", "Java Project").select();
        newProjectDialogBot.button("Next >").click();
        newProjectDialogBot.textWithLabel("Project name:").setText(name);
        newProjectDialogBot.button("Finish").click();
        bot.waitUntil(Conditions.shellCloses(dialog));
    }
    
    /**
     * Deletes the project with the provided name.
     * 
     * @param name The name of the project.
     */
    public static void deleteJavaProject(final String name) {
        final SWTWorkbenchBot bot = new SWTWorkbenchBot();
        bot.viewById("org.eclipse.jdt.ui.PackageExplorer").bot().tree().getTreeItem(name).contextMenu("Delete").click();
        final SWTBotShell dialog = bot.shell("Delete Resources");
        final SWTBot dialogBot = dialog.bot();
        dialogBot.checkBox().select();
        dialogBot.button("OK").click();
        bot.waitUntil(Conditions.shellCloses(dialog));
    }
    
    /**
     * Opens the PMD property dialog of the project with the provided name.
     * 
     * @param name The name of the project.
     * @return A bot for the open PMD property dialog.
     */
    public static PMDProjectPropertyDialogBot openPMDProjectPropertyDialog(final String name) {
        final SWTWorkbenchBot bot = new SWTWorkbenchBot();
        bot.viewById("org.eclipse.jdt.ui.PackageExplorer").bot().tree().getTreeItem(name).select();
        bot.menu("File").menu("Properties").click();
        final PMDProjectPropertyDialogBot dialog = new PMDProjectPropertyDialogBot(bot.shell("Properties for " + name).widget);
        dialog.bot().tree().getTreeItem("PMD").select();
        return dialog;
    }

}