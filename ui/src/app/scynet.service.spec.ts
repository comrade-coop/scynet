import { TestBed } from '@angular/core/testing';

import { ScynetService } from './scynet.service';

describe('ScynetService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ScynetService = TestBed.get(ScynetService);
    expect(service).toBeTruthy();
  });
});
