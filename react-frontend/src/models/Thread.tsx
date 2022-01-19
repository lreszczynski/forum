import { Post } from './Post';
import { User } from './User';

export interface Thread {
  id: number;
  title: String;
  createDate: Date;
  active: boolean;
  pinned: boolean;
  user: User;
  postsCount: number;
  lastPost: Post;
}
