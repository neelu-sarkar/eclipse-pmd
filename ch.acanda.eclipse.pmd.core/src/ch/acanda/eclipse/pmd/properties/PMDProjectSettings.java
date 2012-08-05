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

package ch.acanda.eclipse.pmd.properties;

import static com.google.common.base.Strings.nullToEmpty;

import java.util.Set;

import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleSetReferenceId;
import net.sourceforge.pmd.RuleSets;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import ch.acanda.eclipse.pmd.PMDPlugin;
import ch.acanda.eclipse.pmd.builder.PMDNature;
import ch.acanda.eclipse.pmd.domain.RuleSetConfiguration;
import ch.acanda.eclipse.pmd.preferences.PMDWorkspaceSettings;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Convenience class for reading and writing the PMD settings of a project. See {@link PMDWorkspaceSettings} for
 * settings that do not depend on a specific project.
 * 
 * @author Philip Graf
 */
public final class PMDProjectSettings {
    
    private static final QualifiedName RULE_SETS = new QualifiedName(PMDPlugin.ID, "rulesets");
    private static final QualifiedName ACTIVE_RULE_SET_IDS = new QualifiedName(PMDPlugin.ID, "activerulesets");
    
    private static final Function<RuleSetConfiguration, RuleSetReferenceId> TO_REFERENCE_ID = new Function<RuleSetConfiguration, RuleSetReferenceId>() {
        @Override
        public RuleSetReferenceId apply(final RuleSetConfiguration config) {
            return new RuleSetReferenceId(config.getLocation());
        }
    };
    
    private static final Function<RuleSetConfiguration, Integer> TO_RULESETCONFIGURATION_ID = new Function<RuleSetConfiguration, Integer>() {
        @Override
        public Integer apply(final RuleSetConfiguration config) {
            return config.getId();
        }
    };
    
    private final IProject project;
    
    public PMDProjectSettings(final IProject project) {
        assert project != null && project.isOpen();
        this.project = project;
    }
    
    public RuleSets getRuleSets() {
        RuleSets ruleSets = null;
        try {
            ruleSets = (RuleSets) project.getSessionProperty(RULE_SETS);
            if (ruleSets == null) {
                final PMDWorkspaceSettings workspaceSettings = new PMDWorkspaceSettings(PMDPlugin.getDefault().getPreferenceStore());
                final ImmutableList<RuleSetConfiguration> configs = workspaceSettings.getRuleSetsConfigurations();
                ruleSets = new RuleSetFactory().createRuleSets(Lists.transform(configs, TO_REFERENCE_ID));
            }
        } catch (final CoreException | RuleSetNotFoundException e) {
            PMDPlugin.getDefault().error("Could not load PMD rule sets.", e);
        }
        if (ruleSets == null) {
            PMDPlugin.getDefault().error("Could not load PMD rule sets.", null);
            ruleSets = new RuleSets();
        }
        return ruleSets;
    }
    
    /**
     * @return {@code true} if the project has the PMD nature, {@code false} otherwise.
     */
    public boolean isPMDEnabled() {
        try {
            return project.hasNature(PMDNature.ID);
        } catch (final CoreException e) {
            PMDPlugin.getDefault().error("Cannot access PMD nature of project " + project.getName(), e);
        }
        return false;
    }
    
    /**
     * Adds or removes the PDM nature from the project.
     */
    public void setPMDEnabled(final boolean isEnabled) {
        try {
            if (isEnabled) {
                PMDNature.addTo(project);
            } else {
                PMDNature.removeFrom(project);
            }
        } catch (final CoreException e) {
            PMDPlugin.getDefault().error("Cannot change PMD nature of project " + project.getName(), e);
        }
    }
    
    /**
     * Stores the ids of the active rule set configurations.
     */
    public void setActiveRuleSetConfigurations(final Set<RuleSetConfiguration> activeConfigurations) {
        try {
            final Iterable<Integer> ids = Iterables.transform(activeConfigurations, TO_RULESETCONFIGURATION_ID);
            project.setPersistentProperty(ACTIVE_RULE_SET_IDS, Joiner.on(',').join(ids));
            // reset the cached rule sets
            project.setSessionProperty(RULE_SETS, null);
        } catch (final CoreException e) {
            PMDPlugin.getDefault().error("Cannot store ids of active rule set configurations of project " + project.getName(), e);
        }
    }
    
    public Set<RuleSetConfiguration> getActiveRuleSetConfigurations(final ImmutableList<RuleSetConfiguration> configs) {
        final ImmutableSet.Builder<RuleSetConfiguration> activeConfigs = ImmutableSet.builder();
        try {
            final ImmutableSet.Builder<Integer> idsBuilder = ImmutableSet.builder();
            final String activeRuleSetIds = nullToEmpty(project.getPersistentProperty(ACTIVE_RULE_SET_IDS));
            for (final String id : Splitter.on(',').omitEmptyStrings().split(activeRuleSetIds)) {
                idsBuilder.add(Integer.parseInt(id));
            }
            final ImmutableSet<Integer> ids = idsBuilder.build();
            for (final RuleSetConfiguration config : configs) {
                if (ids.contains(config.getId())) {
                    activeConfigs.add(config);
                }
            }
        } catch (final CoreException e) {
            PMDPlugin.getDefault().error("Cannot retrieve active rule set configuration ids of project " + project.getName(), e);
        }
        return activeConfigs.build();
    }
    
}