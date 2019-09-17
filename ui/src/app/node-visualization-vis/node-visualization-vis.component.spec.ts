import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NodeVisualizationVisComponent } from './node-visualization-vis.component';

describe('NodeVisualizationVisComponent', () => {
  let component: NodeVisualizationVisComponent;
  let fixture: ComponentFixture<NodeVisualizationVisComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NodeVisualizationVisComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NodeVisualizationVisComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
