import { Role } from 'models/Role';
import api from './Api';

const RoleService = {
  async getAllRoles(): Promise<Role[]> {
    const value = await api.get<Role[]>('/roles', {
      // headers: authHeader(),
    });
    return value.data;
  },

  async getRoleById(id: number): Promise<Role> {
    const value = await api.get<Role>(`/roles/${id}`);
    return value.data;
  },
};

export default RoleService;
// rules={[{ min: 10, max: 10000, required: true }]}
