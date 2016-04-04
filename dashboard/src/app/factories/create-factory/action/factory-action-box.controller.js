/*
 *  [2015] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
'use strict';

/**
 * Defines controller of directive for displaying action box.
 * @author Florent Benoit
 */
export class FactoryActionBoxCtrl {


  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($mdDialog) {
    this.$mdDialog = $mdDialog;
  }

  /**
   * User clicked on the + button to add a new action. Show the dialog
   * @param $event
   */
  addAction($event) {
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'FactoryActionDialogAddCtrl',
      controllerAs: 'factoryActionDialogAddCtrl',
      bindToController: true,
      clickOutsideToClose: true,
      locals: { callbackController: this},
      templateUrl: 'app/factories/create-factory/action/factory-action-widget-dialog-add.html'
    });
  }


  callbackAddAction(actionKey, actionParam) {
    console.log('Adding the action with key', actionKey, 'and param', actionParam);

    if (!this.factoryObject.ide) {
      this.factoryObject.ide = {};
    }
    if (!this.factoryObject.ide[this.lifecycle]) {
        this.factoryObject.ide[this.lifecycle] = {};
        this.factoryObject.ide[this.lifecycle].actions = [];
    }

    var actionToAdd;
    if ('openfile' === actionKey) {
      actionToAdd = {
        "properties": {
          "file": actionParam
        },
        "id": "openFile"
      };
    } else if ('runcommand' === actionKey) {
      actionToAdd = {
        "properties": {
          "name": actionParam
        },
        "id": "runCommand"
      };
    }

    console.log('actions are', this);
    if (actionToAdd) {
      this.factoryObject.ide[this.lifecycle].actions.push(actionToAdd);
    }
  }
}
