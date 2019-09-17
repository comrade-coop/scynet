import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NodesVisualizationComponent } from './nodes-visualization.component';

describe('NodesVisualizationComponent', () => {
  let component: NodesVisualizationComponent;
  let fixture: ComponentFixture<NodesVisualizationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NodesVisualizationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NodesVisualizationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
