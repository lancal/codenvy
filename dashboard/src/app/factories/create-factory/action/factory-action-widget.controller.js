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
 * Defines controller of directive for displaying action widget.
 * @author Florent Benoit
 */
export class FactoryActionCtrl {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($mdDialog) {
    this.$mdDialog = $mdDialog;
  }

  /**
   * User clicked on the edit button to add a new action. Show the dialog
   * @param $event
   */
  editAction($event) {
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'FactoryActionDialogEditCtrl',
      controllerAs: 'factoryActionDialogEditCtrl',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        callbackController: this,
        selectedValue: this.action.properties[this.actionProperty]
      },
      templateUrl: 'app/factories/create-factory/action/factory-action-widget-dialog-edit.html'
    });
  }

  callbackEditAction(newValue) {
    this.action.properties[this.actionProperty] = newValue;
  }
}
