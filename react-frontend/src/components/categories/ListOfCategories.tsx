import './ListOfCategories.scss';

import { HomeFilled } from '@ant-design/icons';
import { Breadcrumb, Button, Col, notification, Row, Skeleton } from 'antd';
import BreadcrumbItem from 'antd/lib/breadcrumb/BreadcrumbItem';
import Text from 'antd/lib/typography/Text';
import React, { Fragment } from 'react';
import { useQuery } from 'react-query';
import { Link, useNavigate } from 'react-router-dom';
import AuthService from 'services/AuthService';
import CategoryService from 'services/CategoryService';
import SingleCategory from './SingleCategory';

function ListOfCategories() {
  const navigate = useNavigate();
  const canUserCreateCategory = AuthService.canUserCreateCategory();

  const query = useQuery('categories', () =>
    CategoryService.getAllCategories(),
  );

  const breadcrumbs = (
    <Breadcrumb>
      <BreadcrumbItem>
        <Link to="/">
          <HomeFilled />
        </Link>
      </BreadcrumbItem>
      <BreadcrumbItem>
        <span>Forum</span>
      </BreadcrumbItem>
    </Breadcrumb>
  );

  if (query.isLoading) {
    return (
      <>
        {breadcrumbs}
        {Array(3).map((_x, _i) => (
          <Skeleton />
        ))}
      </>
    );
  }

  if (query.isError) {
    notification.error({
      duration: 0,
      message: 'Connection problem',
      description: 'Could not connect to the server.',
      onClick: () => console.log('Notification Clicked!'),
    });
    return (
      <>
        {breadcrumbs}
        <Text>Error!</Text>
      </>
    );
  }

  // useEffect(() => {}, []);

  if (query.isSuccess) {
    return (
      <>
        {breadcrumbs}
        {canUserCreateCategory && (
          <Button
            type="primary"
            style={{ marginBottom: '16px' }}
            onClick={() => navigate(`/categories/new`)}
          >
            Create New Category
          </Button>
        )}
        <Row gutter={[8, 8]}>
          {query.data.map(value => (
            <Col key={value.id} xs={24} lg={12}>
              <SingleCategory category={value} />
            </Col>
          ))}
        </Row>
      </>
    );
  }

  return null;
}

export default ListOfCategories;
