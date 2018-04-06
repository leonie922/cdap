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
import PropTypes from 'prop-types';
import IconSVG from 'components/IconSVG';
import SetPreferenceModal from 'components/FastAction/SetPreferenceAction/SetPreferenceModal';
import classnames from 'classnames';
import {convertMapToKeyValuePairs} from 'services/helpers';
import {MyPreferenceApi} from 'api/preference';

export default class SystemPrefsAccordion extends Component {
  state = {
    prefsModalOpen: false,
    prefsForDisplay: convertMapToKeyValuePairs(this.props.prefs),
    viewAll: false
  };

  static propTypes = {
    prefs: PropTypes.object,
    expanded: PropTypes.bool,
    onExpand: PropTypes.func
  };

  fetchPrefs = () => {
    MyPreferenceApi
      .getSystemPreferences()
      .subscribe(
        (prefs) => {
          this.setState({
            prefsForDisplay: convertMapToKeyValuePairs(prefs),
          });
        },
        (err) => console.log(err)
      );
  }

  togglePrefsModal = () => {
    this.setState({
      prefsModalOpen: !this.state.prefsModalOpen
    });
  }

  toggleViewAll = () => {
    this.setState({
      viewAll: !this.state.viewAll
    });
  }

  renderLabel() {
    return (
      <div
        className="admin-config-container-toggle"
        onClick={this.props.onExpand}
      >
        <span className="admin-config-container-label">
          <IconSVG name={this.props.expanded ? "icon-caret-down" : "icon-caret-right"} />
          <h5>{`System Preferences (${this.state.prefsForDisplay.length})`}</h5>
        </span>
        <span className="admin-config-container-description">
          Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer nec odio. Praesent libero.
        </span>
      </div>
    );
  }

  renderGrid() {
    if (!this.state.prefsForDisplay.length) {
      return (
        <div className="grid-wrapper text-xs-center">
          No System Preferences set
        </div>
      );
    }

    let prefs = [...this.state.prefsForDisplay];

    if (!this.state.viewAll && prefs.length > 5) {
      prefs = prefs.slice(0, 5);
    }

    return (
      <div className="grid-wrapper">
        <div className="grid grid-container">
          <div className="grid-header">
            <div className="grid-row">
              <strong>Key</strong>
              <strong>Value</strong>
            </div>
          </div>
          <div className="grid-body">
            {
              prefs.map((pref, i) => {
                return (
                  <div className="grid-row" key={i}>
                    <div>{pref.key}</div>
                    <div>{pref.value}</div>
                  </div>
                );
              })
            }
          </div>
        </div>
      </div>
    );
  }

  renderContent() {
    if (!this.props.expanded) {
      return null;
    }

    return (
      <div className="admin-config-container-content system-prefs-container-content">
        <button
          className="btn btn-secondary"
          onClick={this.togglePrefsModal}
        >
          Edit System Preferences
        </button>
        {this.renderGrid()}
        {this.renderViewAllLabel()}
        {
          this.state.prefsModalOpen ?
            <SetPreferenceModal
              isOpen={this.state.prefsModalOpen}
              toggleModal={this.togglePrefsModal}
              onSuccess={this.fetchPrefs}
            />
          :
            null
        }
      </div>
    );
  }

  renderViewAllLabel() {
    if (this.state.prefsForDisplay.length <= 5) {
      return null;
    }

    return (
      <span
        className="view-more-label"
        onClick={this.toggleViewAll}
      >
        {
          this.state.viewAll ?
            'View Less'
          :
            'View All'
        }
      </span>
    );
  }

  render() {
    return (
      <div className={classnames(
        "admin-config-container system-prefs-container",
        {"expanded": this.props.expanded}
      )}>
        {this.renderLabel()}
        {this.renderContent()}
      </div>
    );
  }
}
