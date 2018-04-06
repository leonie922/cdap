/*
* Copyright © 2018 Cask Data, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*/

import React, {Component} from 'react';
import ReloadSystemArtifacts from 'components/Administration/AdminConfigTabContent/ReloadSystemArtifacts';
import HttpExecutorLink from 'components/Administration/AdminConfigTabContent/HttpExecutorLink';
import NamespacesAccordion from 'components/Administration/AdminConfigTabContent/NamespacesAccordion';
import SystemProfilesAccordion from 'components/Administration/AdminConfigTabContent/SystemProfilesAccordion';
import LoadingSVGCentered from 'components/LoadingSVGCentered';
import {MyNamespaceApi} from 'api/namespace';
import {MyPreferenceApi} from 'api/preference';
import {MyProfileApi} from 'api/cloud';
import {Observable} from 'rxjs/Observable';

require('./AdminConfigTabContent.scss');

export const ADMIN_CONFIG_ACCORDIONS = {
  namespaces: 'NAMESPACES',
  systemProfiles: 'SYSTEM_PROFILES',
  systemPrefs: 'SYSTEM_PREFS'
};

export default class AdminConfigTabContent extends Component {
  state = {
    namespaces: 0,
    systemProfiles: 0,
    systemPrefs: 0,
    loading: true,
    expandedAccordion: null
  };

  expandAccordion = (accordion) => {
    if (this.state.expandedAccordion === accordion) {
      this.setState({
        expandedAccordion: null
      });
    } else {
      this.setState({
        expandedAccordion: accordion
      });
    }
  }

  componentWillMount() {
    Observable
      .forkJoin(
        MyNamespaceApi.list(),
        MyProfileApi.list({namespace: 'system'}),
        MyPreferenceApi.getSystemPreferences()
      )
      .subscribe(
        (res) => {
          let [namespaces, systemProfiles, systemPrefs] = res;
          this.setState({
            namespaces,
            systemProfiles,
            systemPrefs,
            loading: false
          });
        },
        (err) => console.log(err)
      );
  }

  render() {
    if (this.state.loading) {
      return <LoadingSVGCentered />;
    }
    return (
      <div className="admin-config-tab-content">
        <div className="action-buttons">
          <ReloadSystemArtifacts />
          <HttpExecutorLink />
        </div>
        <NamespacesAccordion
          namespaces={this.state.namespaces}
          expanded={this.state.expandedAccordion === ADMIN_CONFIG_ACCORDIONS.namespaces}
          onExpand={this.expandAccordion.bind(this, ADMIN_CONFIG_ACCORDIONS.namespaces)}
        />
        <SystemProfilesAccordion
          profiles={this.state.systemProfiles}
          expanded={this.state.expandedAccordion === ADMIN_CONFIG_ACCORDIONS.systemProfiles}
          onExpand={this.expandAccordion.bind(this, ADMIN_CONFIG_ACCORDIONS.systemProfiles)}
        />
      </div>
    );
  }
}
