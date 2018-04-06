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

import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {GLOBALS} from 'services/global-constants';
import {humanReadableDate} from 'services/helpers';

require('./Runs.scss');

const PIPELINES = [GLOBALS.etlDataPipeline, GLOBALS.etlDataStreams];

function getName(run) {
  let name = run.application.name;

  if (PIPELINES.indexOf(run.artifact.name) == -1) {
    name = `${name} - ${run.program}`;
  }

  return name;
}

function getType(run) {
  switch (run.artifact.name) {
    case GLOBALS.etlDataPipeline:
      return 'Batch Pipeline';
    case GLOBALS.etlDataStreams:
      return 'Realtime Pipeline';
    default:
      return run.type;
  }
}

function RunsView({runs}) {
  // this table will be having dynamic number of columns, so cannot use css grid
  return (
    <div className="reports-runs-container">
      <table className="table">
        <thead>
          <th>Namespace</th>
          <th>Name</th>
          <th>Type</th>
          <th>Duration</th>
          <th>Start time</th>
          <th>End time</th>
          <th>User</th>
          <th>Start method</th>
          <th># Log errors</th>
          <th># Log warnings</th>
          <th># Records out</th>
        </thead>

        <tbody>
          {
            runs.map((run, i) => {
              // key needs to be changed to runId when info is available
              return (
                <tr key={i}>
                  <td>{run.namespace}</td>
                  <td>{getName(run)}</td>
                  <td>{getType(run)}</td>
                  <td>{run.duration}</td>
                  <td>{humanReadableDate(run.start)}</td>
                  <td>{humanReadableDate(run.end)}</td>
                  <td>{run.user}</td>
                  <td>{run.startMethod}</td>
                  <td>{run.numLogErrors}</td>
                  <td>{run.numLogWarnings}</td>
                  <td>{run.numRecordsOut}</td>
                </tr>
              );
            })
          }
        </tbody>
      </table>
    </div>
  );
}

RunsView.propTypes = {
  runs: PropTypes.array
};

const mapStateToProps = (state) => {
  return {
    runs: state.details.runs
  };
};

const Runs = connect(
  mapStateToProps
)(RunsView);

export default Runs;
