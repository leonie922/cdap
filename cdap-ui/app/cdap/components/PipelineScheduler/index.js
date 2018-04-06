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

import React, { Component } from 'react';
import {Provider} from 'react-redux';
import PropTypes from 'prop-types';
import {MyPipelineApi} from 'api/pipeline';
import PipelineDetailStore from 'components/PipelineDetails/store';
import PipelineSchedulerStore, {ACTIONS as PipelineSchedulerActions} from 'components/PipelineScheduler/Store';
import {setStateFromCron} from 'components/PipelineScheduler/Store/ActionCreator';
import ViewSwitch from 'components/PipelineScheduler/ViewSwitch';
import ViewContainer from 'components/PipelineScheduler/ViewContainer';
import {setSchedule, setMaxConcurrentRuns, setOptionalProperty} from 'components/PipelineDetails/store/ActionCreator';
import IconSVG from 'components/IconSVG';
import {getCurrentNamespace} from 'services/NamespaceStore';
import StatusMapper from 'services/StatusMapper';
import {isDescendant} from 'services/helpers';
import {Observable} from 'rxjs/Observable';
import {PROFILES_DROPDOWN_DOM_CLASS} from 'components/PipelineScheduler/ProfilesForSchedule';
import T from 'i18n-react';

const PREFIX = 'features.PipelineScheduler';

require('./PipelineScheduler.scss');

export default class PipelineScheduler extends Component {
  constructor(props) {
    super(props);

    setStateFromCron(this.props.schedule);
    PipelineSchedulerStore.dispatch({
      type: PipelineSchedulerActions.SET_MAX_CONCURRENT_RUNS,
      payload: {
        maxConcurrentRuns: this.props.maxConcurrentRuns
      }
    });

    this.state = {
      isScheduleChanged: false,
      savingSchedule: false,
      savingAndScheduling: false,
      scheduleStatus: this.props.scheduleStatus
    };

    this.schedulerStoreSubscription = PipelineSchedulerStore.subscribe(() => {
      let state = PipelineSchedulerStore.getState();
      let currentCron = state.cron;
      let curretMaxConcurrentRuns = state.maxConcurrentRuns;
      if ((currentCron !== this.props.schedule || curretMaxConcurrentRuns !== this.props.maxConcurrentRuns) && !this.state.isScheduleChanged) {
        this.setState({
          isScheduleChanged: true
        });
      } else if (currentCron === this.props.schedule && curretMaxConcurrentRuns === this.props.maxConcurrentRuns && this.state.isScheduleChanged) {
        this.setState({
          isScheduleChanged: false
        });
      }
    });
  }

  componentDidMount() {
    if (!this.props.isDetailView) {
      return;
    }

    this.documentClick$ = Observable.fromEvent(document, 'click')
    .subscribe((e) => {
      if (!this.schedulerComponent) {
        return;
      }

      if (
        isDescendant(this.schedulerComponent, e.target) ||
        isDescendant(
          document.querySelector(`body > .${PROFILES_DROPDOWN_DOM_CLASS}.dropdown`),
          e.target
        )
      ) {
        return;
      }

      this.props.onClose();
    });
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.scheduleStatus !== this.state.scheduleStatus) {
      this.setState({
        scheduleStatus: nextProps.scheduleStatus
      });
    }
  }

  componentWillUnmount() {
    PipelineSchedulerStore.dispatch({
      type: PipelineSchedulerActions.RESET
    });
    if (this.schedulerStoreSubscription) {
      this.schedulerStoreSubscription();
    }
    if (this.documentClick$) {
      this.documentClick$.unsubscribe();
    }
  }

  startScheduleAndClose = () => {
    if (this.state.isScheduleChanged) {
      this.saveSchedule(this.state.isScheduleChanged);
    } else {
      this.schedulePipeline();
    }
  };

  schedulePipeline = () => {
    this.props.onClose();
    this.props.schedulePipeline();
  };

  suspendSchedule = () => {
    this.props.onClose();
    this.props.suspendSchedule();
  };

  saveSchedule = (shouldSchedule = false) => {
    let {cron, maxConcurrentRuns} = PipelineSchedulerStore.getState();

    // In Studio mode, which is still using Angular action creator
    if (!this.props.isDetailView) {
      this.props.actionCreator.setSchedule(cron);
      this.props.actionCreator.setMaxConcurrentRuns(maxConcurrentRuns);
      this.props.onClose();
      return;
    }

    let savingState = shouldSchedule ? 'savingAndScheduling' : 'savingSchedule';

    this.setState({
      [savingState]: true
    });

    let {name, description, artifact, config, principal} = PipelineDetailStore.getState();
    config = {
      ...config,
      schedule: cron,
      maxConcurrentRuns,
    };
    MyPipelineApi.publish({
      namespace: getCurrentNamespace(),
      appId: name
    }, {
      name,
      description,
      artifact,
      config,
      principal,
      'app.deploy.update.schedules': true
    })
    .subscribe(() => {
      this.setState({
        [savingState]: false
      });
      this.props.onClose();
      if (shouldSchedule) {
        this.props.schedulePipeline();
      }
    }, (err) => {
      console.log(err);
      this.setState({
        [savingState]: false
      });
    });

    setSchedule(cron);
    setMaxConcurrentRuns(maxConcurrentRuns);
    setOptionalProperty('app.deploy.update.schedules', true);
  };

  renderHeader() {
    return (
      <div className="pipeline-scheduler-header modeless-header">
        <div className="modeless-title">
          {T.translate(`${PREFIX}.header`)}
          {
            this.props.pipelineName.length ?
              ` ${this.props.pipelineName}`
            :
              null
          }
        </div>
        <div className="btn-group">
          <a
            className="btn"
            onClick={this.props.onClose}
          >
            <IconSVG name="icon-close" />
          </a>
        </div>
      </div>
    );
  }

  renderActionButtons() {
    if (this.state.scheduleStatus === StatusMapper.statusMap['SCHEDULED']) {
      return (
        <div className="schedule-navigation">
          <button
            className="btn btn-primary schedule-btn"
            onClick={this.suspendSchedule}
          >
            {T.translate(`${PREFIX}.suspendSchedule`)}
          </button>
        </div>
      );
    }

    if (!this.props.isDetailView) {
      return (
        <div className="schedule-navigation">
          <button
            className="btn btn-primary save-schedule-btn"
            onClick={this.saveSchedule.bind(this, false)}
          >
            <span>
              {T.translate(`${PREFIX}.saveSchedule`)}
            </span>
          </button>
        </div>
      );
    }

    return (
      <div className="schedule-navigation">
        <button
          className="btn btn-primary start-schedule-btn"
          onClick={this.startScheduleAndClose}
          disabled={this.state.savingSchedule || this.state.savingAndScheduling}
        >
          <span>
            {
              this.state.isScheduleChanged ?
                T.translate(`${PREFIX}.saveAndStartSchedule`)
              :
                T.translate(`${PREFIX}.startSchedule`)
            }
            {
               this.state.savingAndScheduling ?
                <IconSVG
                  name="icon-spinner"
                  className="fa-spin"
                />
              :
                null
            }
          </span>
        </button>
        <button
          className="btn btn-secondary start-schedule-btn"
          onClick={this.saveSchedule.bind(this, false)}
          disabled={this.state.savingSchedule || this.state.savingAndScheduling}
        >
          <span>{T.translate(`${PREFIX}.saveSchedule`)}</span>
          {
             this.state.savingSchedule ?
              <IconSVG
                name="icon-spinner"
                className="fa-spin"
              />
            :
              null
          }
        </button>
      </div>
    );
  }

  render() {
    return (
      <Provider store={PipelineSchedulerStore}>
        <div
          className="pipeline-scheduler-content modeless-container"
          ref={(ref) => this.schedulerComponent = ref}
        >
          {this.renderHeader()}
          <div className="pipeline-scheduler-body modeless-content">
            <div className="schedule-content">
                <fieldset disabled={this.state.scheduleStatus === StatusMapper.statusMap['SCHEDULED']}>
                  <ViewSwitch />
                  <ViewContainer />
                </fieldset>
                {this.renderActionButtons()}
            </div>
          </div>
        </div>
      </Provider>
    );
  }
}

PipelineScheduler.propTypes = {
  actionCreator: PropTypes.object,
  schedule: PropTypes.string,
  maxConcurrentRuns: PropTypes.number,
  onClose: PropTypes.func,
  isDetailView: PropTypes.bool,
  pipelineName: PropTypes.string,
  scheduleStatus: PropTypes.string,
  schedulePipeline: PropTypes.func,
  suspendSchedule: PropTypes.func
};
