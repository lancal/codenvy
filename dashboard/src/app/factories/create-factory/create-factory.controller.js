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
 * Controller for a create factory.
 * @author Oleksii Orel
 */
export class CreateFactoryCtrl {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($location, cheAPI, codenvyAPI, cheNotification, $scope, $filter, lodash) {
    this.$location = $location;
    this.cheAPI = cheAPI;
    this.codenvyAPI = codenvyAPI;
    this.cheNotification = cheNotification;
    this.$filter = $filter;
    this.lodash = lodash;

    this.isLoading = false;
    this.isImporting = false;

    // at first, we're in source mode
    this.title = 'New Factory';
    this.flow = 'source';

    this.factoryContent = null;
    $scope.$watch('createFactoryCtrl.factoryContent', (newValue) => {
      this.factoryObject = angular.fromJson(newValue);
    }, true);


    $scope.$watch('createFactoryCtrl.factoryObject', (newValue) => {
      this.factoryContent = this.$filter('json')(angular.fromJson(this.factoryObject));
    }, true);


    $scope.$watch('createFactoryCtrl.gitLocation', (newValue) => {
      // update underlying model
      // Updating first project item
      if (!this.factoryObject) {
        //fetch it !
        let templateName = 'git';
        let promise = this.codenvyAPI.getFactoryTemplate().fetchFactoryTemplate(templateName);

        promise.then(() => {
          let factoryContent = this.codenvyAPI.getFactoryTemplate().getFactoryTemplate(templateName);
          this.factoryObject = angular.fromJson(factoryContent);
          this.updateGitProjectLocation(newValue);
        });
      } else {
        this.updateGitProjectLocation(newValue);
      }

    }, true);
  }

  /**
   * Update the source project location for git
   * @param location the new location
     */
  updateGitProjectLocation(location) {
    let project = this.factoryObject.workspace.projects[0];
    project.source.type = 'git';
    project.source.location = location;
  }

  /**
   * Update the machine recipe URL
   * @param recipeURL
   */
  updateMachineRecipeLocation(recipeURL) {
    if (!this.factoryObject) {
      return;
    }
    let machineConfig = this.factoryObject.workspace.environments[0].machineConfigs[0];
    machineConfig.source.type = 'recipe';
    machineConfig.source.location = recipeURL;
  }

  /**
   * Create a new factory by factory content
   * @param factoryContent
   */
  createFactoryByContent(factoryContent) {
    if (!factoryContent) {
      return;
    }

    // go into configure mode
    this.flow = 'configure';

    this.title = 'Configure Factory';

    this.isImporting = true;

    let promise = this.codenvyAPI.getFactory().createFactoryByContent(factoryContent);

    promise.then((factory) => {
      this.isImporting = false;

      this.lodash.find(factory.links, (link) => {
        if (link.rel === 'accept' || link.rel === 'accept-named') {
          this.factoryLink = link.href;
        }
      });

      var parser = document.createElement('a');
      parser.href = this.factoryLink;

      this.factoryBadgeUrl = parser.protocol + '//' + parser.hostname + '/factory/resources/codenvy-contribute.svg';

      this.markdown = '[![Contribute](' + this.factoryBadgeUrl + ')](' + this.factoryLink + ')';
    }, (error) => {
      this.isImporting = false;
      this.cheNotification.showError(error.data.message ? error.data.message : 'Create factory failed.');
      console.log('error', error);
    });
  }

  /**
   * Flow of creating a factory is finished, we can redirect to details of factory
   */
  finishFlow() {
    this.cheNotification.showInfo('Factory successfully created.');
    this.$location.path('/factory/' + this.factoryObject.id);
  }

  setStackTab(tabName) {

    console.log('selecting stack named', tabName);
    console.log('selecting stack with stack =', this.stack);
  }


  /**
   * Callback when stack has been set
   * @param stack  the selected stack
   */
  cheStackLibrarySelecter(stack) {
    console.log('selecting stack which is', stack);
    console.log('selecting recipeScript which is', this.recipeScript);
    console.log('selecting recipeUrl which is', this.recipeUrl);
    this.recipeUrl = null;
    this.isCustomStack = false;
    this.stack = stack;


    //check predefined recipe location
    if (this.stack && this.stack.source && this.stack.source.type === 'location') {
      this.updateMachineRecipeLocation(stack.source.origin);
    } else if (this.stack) {
      // needs to get recipe URL from stack
      let promise = this.computeRecipeForStack(this.stack);
      promise.then((recipe) => {
        let findLink = this.lodash.find(recipe.links, function (link) {
          return link.rel === 'get recipe script';
        });
        if (findLink) {
          this.updateMachineRecipeLocation(findLink.href);
        }
      }, (error) => {
        this.cheNotification.showError(error.data.message ? error.data.message : 'Error during recipe creation.');
      });
    }
  }

  /**
   * User has selected a stack. needs to find or add recipe for that stack
   */
  computeRecipeForStack(stack) {
    // look at recipe
    let recipeSource = stack.source;

    let promise;

    // what is type of source ?
    if ('image' === recipeSource.type) {
      // needs to add recipe for that script
      promise = this.submitRecipe('generated-' + stack.name, 'FROM ' + recipeSource.origin);
    } else if ('recipe' === recipeSource.type) {

      promise = this.submitRecipe('generated-' + stack.name, recipeSource.origin);
    } else {
      throw 'Not implemented';
    }

    return promise;
  }

  submitRecipe(recipeName, recipeScript) {
    let recipe = {
      type: 'docker',
      name: recipeName,
      permissions: {
        groups: [
          {
            name: 'public',
            acl: [
              'read'
            ]
          }
        ],
        users: {}
      },
      script: recipeScript
    };

    return this.cheAPI.getRecipe().create(recipe);
  }


  selectGitHubRepository() {
    console.log('internal value is', this.selectedGitHubRepository);
    // update location
    this.gitLocation = this.selectedGitHubRepository.clone_url;


  }

  removeAction(actionSection, action) {
    // search action in the section
    let section = this.factoryObject.ide[actionSection];
    if (section) {
      let actions = section.actions;
      if (actions) {
        var index = 0;
        actions.forEach((existingAction) => {
          if (existingAction === action) {
            actions = actions.splice(index, 1);
          }
          index++;
        });
      }
    }
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

}
