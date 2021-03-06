/* eslint-disable no-restricted-syntax */
import {
  Button,
  Checkbox,
  Form,
  Input,
  InputNumber,
  Select,
  Space,
} from 'antd';
import TextArea from 'antd/lib/input/TextArea';
import Title from 'antd/lib/typography/Title';
import { AxiosError } from 'axios';
import * as React from 'react';
import { useMutation, useQuery, useQueryClient } from 'react-query';
import { useNavigate, useParams } from 'react-router-dom';
import { Category } from 'models/Category';
import { Role } from 'models/Role';
import CategoryService from 'services/CategoryService';
import RoleService from 'services/RoleService';
import {
  notificationError,
  notificationErrorStatusCode,
  notificationSuccessfulEdit,
} from 'utils/Notifications';

export interface ICategoryDashboardProps {}

export default function CategoryDashboard(_props: ICategoryDashboardProps) {
  const params = useParams();
  const id = Number(params.id);
  const queryClient = useQueryClient();
  const query = useQuery(`categories/${id}`, () =>
    CategoryService.getCategoryById(id),
  );
  const queryRoles = useQuery(`roles/`, () => RoleService.getAllRoles());
  const mutation = useMutation<Category, AxiosError, Category, any>(
    (newCategory: Category) => CategoryService.updateCategory(newCategory),
  );
  const queries = [query, queryRoles];

  const leftSpan = 4;
  const navigate = useNavigate();

  const update = async (values: any) => {
    const formValues = values;
    formValues.roles = [];
    (formValues.rolesIds as String[])
      .map(r => Number(r))
      .forEach(r => {
        (formValues.roles as Role[]).push(
          queryRoles.data?.find(qr => qr.id === r)!,
        );
      });
    delete formValues.rolesIds;

    const newCategory: Category = values;

    mutation.mutate(newCategory, {
      onSuccess: (_data, _variables) => {
        queryClient.invalidateQueries();
        notificationSuccessfulEdit();
        navigate(-1);
      },
      onError: (error, _variables) => {
        if (error.response !== undefined) {
          notificationErrorStatusCode(error.response.status);
        }
      },
    });
  };

  if (queries.some(q => q.isError)) {
    notificationError();
  }

  if (query.isSuccess && queryRoles.isSuccess) {
    console.log(query.data);

    return (
      <>
        <Title level={2} style={{ textAlign: 'center' }}>
          Category
        </Title>
        <Form
          name="basic"
          initialValues={{
            id: query.data.id,
            name: query.data.name,
            description: query.data.description,
            active: query.data.active,
            rolesIds: query.data.roles.map(r => String(r.id)),
            remember: true,
          }}
          onFinish={update}
          labelCol={{ span: leftSpan }}
          wrapperCol={{ span: 24 - 2 * leftSpan }}
        >
          <Form.Item label="Id" name="id">
            <InputNumber disabled />
          </Form.Item>
          <Form.Item label="Name" name="name">
            <Input />
          </Form.Item>
          <Form.Item label="Description" name="description">
            <TextArea />
          </Form.Item>
          <Form.Item label="Active" name="active" valuePropName="checked">
            <Checkbox />
          </Form.Item>
          <Form.Item
            label="Roles"
            name={['rolesIds']}
            /* normalize={(e: string[]) => {
              console.log('nn', e, typeof e);
              return e;
            }} */
          >
            <Select
              mode="multiple"
              allowClear
              style={{ width: '100%' }}
              placeholder="Please select"
              // defaultValue={selected.map(q => q.name)}
            >
              {queryRoles.data.map(qr => (
                <Select.Option key={qr.id}>{qr.name}</Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item wrapperCol={{ sm: { offset: leftSpan } }}>
            <Space>
              <Button type="primary" htmlType="submit">
                Save
              </Button>
              <Button
                htmlType="button"
                onClick={() => {
                  navigate(-1);
                }}
              >
                Cancel
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </>
    );
  }
  return null;
}
