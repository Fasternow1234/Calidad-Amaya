import '@angular/compiler';
import { FormControl, Validators } from '@angular/forms';
import { describe, expect, it } from 'vitest';
import { cantidadEnteraPositiva } from './cantidad-entera.validator';

describe('cantidadEnteraPositiva', () => {
  const validarCantidad = (valor: number | null) => {
    const control = new FormControl(valor, [
      Validators.required,
      Validators.min(1),
      cantidadEnteraPositiva,
    ]);
    return control;
  };

  it('acepta una cantidad entera positiva', () => {
    expect(validarCantidad(2).valid).toBe(true);
  });

  it('rechaza cantidades fraccionarias', () => {
    expect(validarCantidad(1.5).hasError('cantidadEnteraPositiva')).toBe(true);
  });

  it('rechaza cero y valores negativos', () => {
    expect(validarCantidad(0).valid).toBe(false);
    expect(validarCantidad(-1).valid).toBe(false);
  });

  it('rechaza una cantidad vacía', () => {
    expect(validarCantidad(null).hasError('required')).toBe(true);
  });
});
