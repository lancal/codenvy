/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * Defines a directive for creating factory from git.
 * @author Florent Benoit
 */
export class CreateFactoryGit {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {

    this.controller = 'CreateFactoryGitCtrl';
    this.controllerAs = 'createFactoryGitCtrl';
    this.bindToController = true;

    this.restrict = 'E';
    this.templateUrl = 'app/factories/create-factory/git/create-factory-git.html';


    // scope values
    this.scope = {
      location: '=cdvyGitLocation'
    };
  }

}
