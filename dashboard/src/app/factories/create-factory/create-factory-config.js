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


import {CreateFactoryCtrl} from '../create-factory/create-factory.controller';

import {FactoryFromWorkspaceCtrl} from '../create-factory/workspaces-tab/factory-from-workpsace.controller.js';
import {FactoryFromWorkspace} from '../create-factory/workspaces-tab/factory-from-workspace.directive.js';
import {FactoryFromFileCtrl} from '../create-factory/config-file-tab/factory-from-file.controller';
import {FactoryFromFile} from '../create-factory/config-file-tab/factory-from-file.directive';
import {FactoryFromTemplateCtrl} from '../create-factory/template-tab/factory-from-template.controller';
import {FactoryFromTemplate} from '../create-factory/template-tab/factory-from-template.directive';
import {FactoryActionCtrl} from './action/factory-action-widget.controller';
import {FactoryAction} from './action/factory-action-widget.directive';
import {FactoryActionBoxCtrl} from './action/factory-action-box.controller';
import {FactoryActionBox} from './action/factory-action-box.directive';
import {FactoryActionDialogAddCtrl} from './action/factory-action-widget-dialog-add.controller';
import {FactoryActionDialogEditCtrl} from './action/factory-action-widget-dialog-edit.controller';
import {FactoryCommandCtrl} from './command/factory-command.controller';
import {FactoryCommand} from './command/factory-command.directive';
import {FactoryCommandDialogEditCtrl} from './command/factory-command-edit.controller';
import {CreateFactoryGitCtrl} from './git/create-factory-git.controller';
import {CreateFactoryGit} from './git/create-factory-git.directive';

export class CreateFactoryConfig {

  constructor(register) {

    register.controller('CreateFactoryCtrl', CreateFactoryCtrl);

    register.controller('FactoryFromWorkspaceCtrl', FactoryFromWorkspaceCtrl);
    register.directive('cdvyFactoryFromWorkspace', FactoryFromWorkspace);

    register.controller('FactoryFromFileCtrl', FactoryFromFileCtrl);
    register.directive('cdvyFactoryFromFile', FactoryFromFile);

    register.controller('FactoryFromTemplateCtrl', FactoryFromTemplateCtrl);
    register.directive('cdvyFactoryFromTemplate', FactoryFromTemplate);

    register.controller('FactoryActionBoxCtrl', FactoryActionBoxCtrl);
    register.directive('cdvyFactoryActionBox', FactoryActionBox);

    register.controller('FactoryActionCtrl', FactoryActionCtrl);
    register.directive('cdvyFactoryAction', FactoryAction);

    register.controller('FactoryCommandCtrl', FactoryCommandCtrl);
    register.directive('cdvyFactoryCommand', FactoryCommand);

    register.controller('CreateFactoryGitCtrl', CreateFactoryGitCtrl);
    register.directive('cdvyCreateFactoryGit', CreateFactoryGit);

    register.controller('FactoryActionDialogAddCtrl', FactoryActionDialogAddCtrl);
    register.controller('FactoryActionDialogEditCtrl', FactoryActionDialogEditCtrl);
    register.controller('FactoryCommandDialogEditCtrl', FactoryCommandDialogEditCtrl);



    // config routes
    register.app.config(function ($routeProvider) {
      $routeProvider.accessWhen('/factories/create-factory', {
        templateUrl: 'app/factories/create-factory/create-factory.html',
        controller: 'CreateFactoryCtrl',
        controllerAs: 'createFactoryCtrl'
      });

    });

  }
}
