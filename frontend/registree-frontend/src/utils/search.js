import builder from '@rsql/builder';
import { emit as rsqlEmit } from '@rsql/emitter';

export function toRsql(criteria) {
  const filters = Object.entries(criteria)
    .filter(([_, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => builder.eq(key, value));

  return filters.length > 0 ? rsqlEmit(builder.and(...filters)) : '';
}
