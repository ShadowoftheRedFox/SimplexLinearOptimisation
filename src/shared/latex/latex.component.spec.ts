import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LatexComponent } from './latex.component';

describe('LatexComponent', () => {
  let component: LatexComponent;
  let fixture: ComponentFixture<LatexComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LatexComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LatexComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
